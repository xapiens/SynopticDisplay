// (c) 2001-2010 Fermi Research Alliance
// $Id: AccessLogger.java,v 1.3 2010/09/15 16:14:05 apetrov Exp $
package gov.fnal.controls.webapps.syndi;

import gov.fnal.controls.applications.syndi.runtime.SvgDisplayManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author  Andrey Petrov, Tim Bolshakov
 * @version $Date: 2010/09/15 16:14:05 $
 */
public class AccessLogger implements Filter {
    
    private static final Logger log = Logger.getLogger( AccessLogger.class.getName());
    
    private final Set<String> proxies = new HashSet<String>();
    private final Map<Key,LogHandle> cache = new ConcurrentSkipListMap<Key,LogHandle>();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    private LogDb logDb;
    
    public AccessLogger() {}

    @Override
    public void init( FilterConfig config ) throws ServletException {

        ServletContext ctx = config.getServletContext();
        
        proxies.clear();
        String str = System.getProperty( "Synoptic.proxies" );
        if (str != null) {
            StringBuilder buf = new StringBuilder( "HTTP proxies:" );
            for (StringTokenizer z = new StringTokenizer( str, ";, " ); z.hasMoreTokens();) {
                String s = z.nextToken();
                try {
                    s = InetAddress.getByName( s ).getHostAddress();
                    proxies.add( s );
                    buf.append( "\n    " );
                    buf.append( s );
                } catch (UnknownHostException ex) {
                    log.warning( "Unknown proxy " + s );
                }
            }
            log.config( buf.toString());
        }
        
        logDb = (LogDb)ctx.getAttribute( "log-db" );
        if (logDb == null) {
            log.config( "Logging database is not available" );
        }
        
        worker.scheduleAtFixedRate(
            new Reaper(), 
            SvgDisplayManager.MAX_IDLE_TIME,
            SvgDisplayManager.MAX_IDLE_TIME / 2,
            TimeUnit.MILLISECONDS
        );

        log.info( "Initialized " + this );

        
    }
    
    @Override
    public void destroy() {
        
        worker.shutdownNow();

        for (LogHandle h : cache.values()) {
            logDb.endAccess( h.logId );
        }
        
        cache.clear();
        proxies.clear();
        logDb = null;

        log.info( "Destroyed " + this );

    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    @Override
    public void doFilter( ServletRequest req, ServletResponse res, FilterChain flt ) 
            throws IOException, ServletException {

        String address = req.getRemoteAddr();
        if (proxies.contains( address )) {
            String addr2 = ((HttpServletRequest)req).getHeader( "x-forwarded-for" );
            if (addr2 != null) {
                address = addr2;
            }
        }
        req.setAttribute( "REMOTE-ADDRESS", address );
        
        flt.doFilter( req, res );
        
        if (logDb == null) {
            return;
        }
        
        String dispName = (String)req.getAttribute( "DISPLAY_NAME" );
        if (dispName == null) {
            // DISPLAY_NAME attribute is set by DisplayServlet only if this HTTP
            // request needs to be logged.
            return;
        }
        
        Key key = new Key( dispName, address );
        
        LogHandle handle = cache.get( key );
        
        if (handle == null) {
            Integer logId = logDb.startAccess( dispName, address );
            handle = new LogHandle( logId );
            cache.put( key, handle );
            log.fine( "Started access: " + key );
        } else {
            handle.tick = System.currentTimeMillis();
        }
        
    }
    
    private class Reaper implements Runnable {

        @Override
        public void run() {
            long t0 = System.currentTimeMillis() - SvgDisplayManager.MAX_IDLE_TIME;
            for (Iterator<Entry<Key,LogHandle>> z = cache.entrySet().iterator(); z.hasNext();) {
                Entry<Key,LogHandle> e = z.next();
                if (cache.get( e.getKey()).tick < t0) {
                    logDb.endAccess( e.getValue().logId );
                    log.fine( "Stopped access: " + e.getKey());
                    z.remove();
                } else {
                    logDb.acknowledgeAccess( e.getValue().logId );
                }
            }
        }
        
    }
    
    private class Key implements Comparable<Key> {
        
        private final String dispName, address;
        
        Key( String dispName, String address ) {
            this.dispName = dispName;
            this.address = address;
        }

        @Override
        public int hashCode() {
            return dispName.hashCode() ^ address.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            return obj instanceof Key
                    && ((Key)obj).dispName.equals( dispName )
                    && ((Key)obj).address.equals( address );
        }

        @Override
        public int compareTo( Key obj ) {
            int res = dispName.compareTo( obj.dispName );
            if (res == 0) {
                res = address.compareTo( obj.address );
            }
            return res;
        }

        @Override
        public String toString() {
            return address + " to " + dispName;
        }
        
    }
    
    private class LogHandle {
        
        private final int logId;
        private long tick = System.currentTimeMillis();
        
        LogHandle( int logId ) {
            this.logId = logId;
        }
        
    }
    
}
