// (c) 2001-2010 Fermi Research Allaince
// $Id: Restarter.java,v 1.3 2010/09/15 18:43:05 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 18:43:05 $
 */
class Restarter implements Runnable {

    /*
    private static final String[] RESTART_PATHS = {
        "/usr/home/console/.vnc/restart_tomcat >/dev/null 2>&1 &",
        "/usr/local/tomcat/bin/restart.sh >/dev/null 2>&1 &"
    };
    */

    private static final String RESTART_COMMAND = System.getProperty( "Synoptic.restart-command" );
    
    private static final Logger log = Logger.getLogger( Restarter.class.getName());
    
    public static void restartServer() {
        Thread t = new Thread( new Restarter());
        t.setDaemon( true );
        t.start();
    }
    
    private Restarter() {}

    @Override
    public void run() {
        log.info( "Restarting the server" );
        try {
            if (RESTART_COMMAND == null) {
                throw new Exception( "Restart command is not specified" );
            }
            Runtime.getRuntime().exec( RESTART_COMMAND );
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Cannot restart the server", ex );
        }
    }

    
}
