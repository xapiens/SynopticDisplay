// (c) 2001-2010 Fermi Research Alliance
// $Id: SynopticWebapp.java,v 1.3 2010/09/15 16:14:05 apetrov Exp $
package gov.fnal.controls.webapps.syndi;

import gov.fnal.controls.applications.syndi.Synoptic;
import gov.fnal.controls.applications.syndi.SynopticConfig;
import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author  Andrey Petrov, Tim Bolshakov
 * @version $Date: 2010/09/15 16:14:05 $
 */
public class SynopticWebapp extends Synoptic implements ServletContextListener {

    private static final Logger log = Logger.getLogger( SynopticWebapp.class.getName());

    public SynopticWebapp() {}

    @Override
    public void contextInitialized( ServletContextEvent evt ) {
        ServletContext ctx = evt.getServletContext();
        try {
            URL u = ctx.getResource( "/WEB-INF/config.xml" );
            if (u == null) {
                log.config( "Webapp configuration file doesn't exist" );
            }
            SynopticConfig.getInstance().load( u );
        } catch (Exception ex) {
            throw new RuntimeException( "Cannot load webapp configuration file", ex );
        }
        log.info( "Initialized " + this );
    }

    @Override
    public void contextDestroyed( ServletContextEvent evt ) {
        log.info( "Destroyed " + this );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
