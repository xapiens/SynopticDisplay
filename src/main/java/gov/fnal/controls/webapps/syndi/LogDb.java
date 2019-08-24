// (c) 2001-2010 Fermi Research Alliance
// $Id: LogDb.java,v 1.3 2010/09/15 16:14:05 apetrov Exp $
package gov.fnal.controls.webapps.syndi;

import gov.fnal.controls.applications.syndi.runtime.Quarantine;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Logging database facade.
 * 
 * @author Andrey Petrov
 * @version $Revision: 1.3 $
 */
public class LogDb implements ServletContextListener, Quarantine {

    private static final Pattern PRIVATE_IP = Pattern.compile(
        "10\\..*" +
        "|172\\.(16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31)\\..*" +
        "|192\\.168\\.27\\..*"
    );

    private static boolean isPrivate( String address ) {
        return PRIVATE_IP.matcher( address ).matches();
    }

    private static String truncateAddress( String address ) {
        if (address == null) {
            return null;
        }
        String last = "";
        for (String n : address.split( "," )) {
            last = n;
            if (!isPrivate( n )) {
                return n;
            }
        }
        return last;
    }

    private static final String DB_URL = 
            "jdbc:derby:" + 
            System.getProperty( "catalina.home" ) + "/work/Catalina/localhost/synoptic/stat" +
            ";create=true";

    private static final int STAT_KEEP_TIME = 7; // DAYS
    private static final int ACCESS_KEEP_TIME = 7; // DAYS
    
    private static final int TABLE_COUNT = 3;
    
    private static final String CHECK_SQL =
        "SELECT COUNT(*) FROM SYS.SYSTABLES " +
            "WHERE TABLETYPE='T' AND TABLENAME LIKE 'SYN%'";
    
    private static final String CLEAN_SQL =
        "DROP TABLE SYN_ACCESS;" +
        "DROP TABLE SYN_STAT;" +
        "DROP TABLE SYN_QUARANTINE";
    
    private static final String INIT_SQL = 
            
        "CREATE TABLE SYN_ACCESS(" +
            "ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
            "DISPLAY VARCHAR(1024) NOT NULL," +
            "HOST VARCHAR(15) NOT NULL," +
            "START_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "END_TIME TIMESTAMP," +
            "HEARTBEAT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);" +
        "CREATE INDEX SYN_ACCESS_DISPLAY_INDEX ON SYN_ACCESS(DISPLAY);" +
        "CREATE INDEX SYN_ACCESS_HOST_INDEX ON SYN_ACCESS(HOST);" +
        "CREATE INDEX SYN_SCCESS_START_INDEX ON SYN_ACCESS(START_TIME);" +
        
        "CREATE TABLE SYN_STAT(" +
            "INSTANT TIMESTAMP PRIMARY KEY DEFAULT CURRENT_TIMESTAMP," +
            "HEAP_SIZE INT," +
            "SVG_REQ_COUNT INT," +
            "BMP_REQ_COUNT INT," +
            "DISPLAY_COUNT INT," +
            "START_COUNT INT," +
            "STOP_COUNT INT," +
            "ABORT_COUNT INT," +
            "WORK_TIME INT);" +
            
        "CREATE TABLE SYN_QUARANTINE(" +
            "ADD_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "DISPLAY VARCHAR(1024) NOT NULL);" +
        "CREATE INDEX SYN_QUARANTINE_DISPLAY_INDEX ON SYN_QUARANTINE(DISPLAY);";
    
    private static final String INSERT_NULL_STAT =
        "INSERT INTO SYN_STAT(HEAP_SIZE,SVG_REQ_COUNT,BMP_REQ_COUNT,DISPLAY_COUNT," +
            "START_COUNT,STOP_COUNT,ABORT_COUNT,WORK_TIME) " +
            "VALUES(NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL)";

    private static final String INSERT_STAT =
        "INSERT INTO SYN_STAT(HEAP_SIZE,SVG_REQ_COUNT,BMP_REQ_COUNT,DISPLAY_COUNT," +
            "START_COUNT,STOP_COUNT,ABORT_COUNT,WORK_TIME) " +
            "VALUES(%d,%d,%d,%d,%d,%d,%d,%d)";
    
    private static final String DELETE_STAT =
        "DELETE FROM SYN_STAT " +
            "WHERE INSTANT<{FN TIMESTAMPADD(SQL_TSI_DAY,-" + STAT_KEEP_TIME + ",CURRENT_TIMESTAMP)}";
    
    private static final String INSERT_QUARANTINE =
        "INSERT INTO SYN_QUARANTINE(DISPLAY) VALUES('%s')";
    
    private static final String SELECT_QUARANTINE =
        "SELECT COUNT(*) FROM SYN_QUARANTINE WHERE DISPLAY='%s'";
    
    private static final String INSERT_START_ACCESS =
        "INSERT INTO SYN_ACCESS(DISPLAY,HOST) VALUES('%s','%s')";

    private static final String INSERT_END_ACCESS =
        "UPDATE SYN_ACCESS SET END_TIME=CURRENT_TIMESTAMP WHERE ID=%d";

    private static final String INSERT_ACK_ACCESS =
        "UPDATE SYN_ACCESS SET HEARTBEAT=CURRENT_TIMESTAMP WHERE ID=%d";
    
    private static final String DELETE_ACCESS =
        "DELETE FROM SYN_ACCESS " +
            "WHERE START_TIME<{FN TIMESTAMPADD(SQL_TSI_DAY,-" + ACCESS_KEEP_TIME + ",CURRENT_TIMESTAMP)}";

    private static final Logger log = Logger.getLogger( LogDb.class.getName());
    
    private Connection con;
    
    public LogDb() {} 
            
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection( DB_URL );
    }
    
    @Override
    public void contextInitialized( ServletContextEvent evt ) {
        reconnect();
        if (con != null) {
            reportStartup();
            evt.getServletContext().setAttribute( "log-db", this );
            log.info( "Ilitialized " + this );
        }
    }
    
    @Override
    public void contextDestroyed( ServletContextEvent evt ) {
        evt.getServletContext().removeAttribute( "log-db" );
        shutdown();
        log.info( "Destroyed " + this );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    private synchronized void reconnect() {
        
        Connection con_ = this.con;
        this.con = null;

        if (con_ != null) {
            try {
                con_.close();
            } catch (SQLException ex) {
                log.throwing( getClass().getName(), "reconnect", ex );
            } finally {
                con_ = null;
            }
        }
        
        try {
            Class<?> clazz = Class.forName( "org.apache.derby.jdbc.EmbeddedDriver" );
            DriverManager.registerDriver( (Driver)clazz.newInstance());
        } catch (Exception ex) {
            log.severe( "Cannot initialize logging database: DB driver not found" );
            return;
        }
        
        try {
            con_ = getConnection();
            Statement stt = con_.createStatement();
            try {
                ResultSet rs = stt.executeQuery( CHECK_SQL );
                rs.next();
                int tableCount = rs.getInt( 1 );
                if (tableCount != TABLE_COUNT) {
                    log.info( "Creating new logging database" );
                    if (tableCount != 0) {
                        for (String s : CLEAN_SQL.split( ";" )) {
                            try {
                                stt.execute( s );
                            } catch (SQLException ex) {}
                        }
                    }
                    for (String s : INIT_SQL.split( ";" )) {
                        stt.addBatch( s );
                    }
                    stt.executeBatch();
                }
            } finally {
                stt.close();
            }
        } catch (SQLException ex) {
            shutdown();
            log.log( Level.SEVERE, "Cannot initialize logging database at " + DB_URL, ex );
        }
        
        this.con = con_;
        
    }

    private synchronized void shutdown() {
        
        Connection con_ = this.con;
        this.con = null;
        
        if (con_ != null) {
            try {
                con_.close();
            } catch (SQLException ex) {
                log.throwing( getClass().getName(), "shutdown", ex );
            } finally {
                con_ = null;
            }
        }
        try {
            DriverManager.getConnection( "jdbc:derby:;shutdown=true" );        
        } catch (SQLException ex) { 
            // --- always throws an exception
            //log.throwing( getClass().getName(), "shutdown", ex );
        }
        
    }
    
    private void reportStartup() {
        if (con == null) {
            return;
        }
        try {
            Statement stt = con.createStatement();
            try {
                if (stt.executeUpdate( INSERT_NULL_STAT ) != 1) {
                    log.warning( "Startup record not added" );
                }
            } finally {
                stt.close();
            }
        } catch (SQLException ex) {
            log.log( Level.WARNING, "Cannot add startup record", ex );
            reconnect();
        }
    }
    
    public void stat(
            int heapSize,
            int svgReqCount,
            int bmpReqCount,
            int displayCount,
            int startCount,
            int stopCount,
            int abortCount ) {
        
        if (con == null) {
            return;
        }
        try {
            Statement stt = con.createStatement();
            try {
                String sql = String.format( INSERT_STAT, 
                        heapSize, 
                        svgReqCount, 
                        bmpReqCount,
                        displayCount, 
                        startCount, 
                        stopCount,
                        abortCount,
                        0 // work time: obolete
                );
                if (stt.executeUpdate( sql ) != 1) {
                    log.warning( "Statistics record not added" );
                }
                int n = stt.executeUpdate( DELETE_STAT );
                if (n > 0) {
                    log.fine( "Removed " + n + " old statistics records" );
                }
            } finally {
                stt.close();
            }
        } catch (SQLException ex) {
            log.log( Level.WARNING, "Cannot add statistics record", ex );
            reconnect();
        }
    }
    
    public Integer startAccess( String dispName, String hostName ) {
        if (con == null) {
            return null;
        }
        hostName = truncateAddress( hostName );
        try {
            Statement stt = con.createStatement();
            try {
                String sql = String.format( INSERT_START_ACCESS, dispName, hostName ); 
                if (stt.executeUpdate( sql, Statement.RETURN_GENERATED_KEYS ) != 1) {
                    log.warning( "Access record not added" );
                } else {
                    ResultSet rs = stt.getGeneratedKeys();
                    try {
                        rs.next();
                        return rs.getInt( 1 );
                    } finally {
                        rs.close();
                    }
                }
            } finally {
                stt.close();
            }
        } catch (SQLException ex) {
            log.log( Level.WARNING, "Cannot add access record", ex );
            reconnect();
        }
        return null;
    }
    
    public void endAccess( int id ) {
        if (con == null) {
            return;
        }
        try {
            Statement stt = con.createStatement();
            try {
                String sql = String.format( INSERT_END_ACCESS, id ); 
                if (stt.executeUpdate( sql ) != 1) {
                    log.warning( "Access record not updated" );
                }
                int n = stt.executeUpdate( DELETE_ACCESS );
                if (n > 0) {
                    log.fine( "Removed " + n + " old access records" );
                }
            } finally {
                stt.close();
            }
        } catch (SQLException ex) {
            log.log( Level.WARNING, "Cannot update access record", ex );
            reconnect();
        }
    }
    
    public void acknowledgeAccess( int id ) {
        if (con == null) {
            return;
        }
        try {
            Statement stt = con.createStatement();
            try {
                String sql = String.format( INSERT_ACK_ACCESS, id ); 
                if (stt.executeUpdate( sql ) != 1) {
                    log.warning( "Access record not updated" );
                }
            } finally {
                stt.close();
            }
        } catch (SQLException ex) {
            log.log( Level.WARNING, "Cannot update access record", ex );
            reconnect();
        }
    }

    @Override
    public void quarantine( String dispName ) {
        if (con == null) {
            return;
        }
        try {
            Statement stt = con.createStatement();
            try {
                String sql = String.format( INSERT_QUARANTINE, dispName ); 
                if (stt.executeUpdate( sql ) != 1) {
                    log.warning( "Quarantine record not added" );
                }
            } finally {
                stt.close();
            }
        } catch (SQLException ex) {
            log.log( Level.WARNING, "Cannot add quarantine record", ex );
            reconnect();
        }
    }

    @Override
    public boolean isQuarantined( String dispName ) {
        if (con == null) {
            return false;
        }
        try {
            Statement stt = con.createStatement();
            try {
                String sql = String.format( SELECT_QUARANTINE, dispName ); 
                ResultSet rs = stt.executeQuery( sql );
                try {
                    rs.next();
                    return rs.getInt( 1 ) > 0;
                } finally {
                    rs.close();
                }
            } finally {
                stt.close();
            }
        } catch (SQLException ex) {
            log.log( Level.WARNING, "Cannot check quarantine", ex );
            reconnect(); // reconnect
        }
        return false;
    }
    
}
