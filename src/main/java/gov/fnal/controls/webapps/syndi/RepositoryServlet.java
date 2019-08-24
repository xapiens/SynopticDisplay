// (c) 2001-2010 Fermi Research Alliance
// $Id: RepositoryServlet.java,v 1.4 2010/09/15 16:14:05 apetrov Exp $
package gov.fnal.controls.webapps.syndi;

import gov.fnal.controls.applications.syndi.repository.FileRepositoryAccess;
import gov.fnal.controls.applications.syndi.repository.RepositoryAccess;
import gov.fnal.controls.applications.syndi.runtime.TimedDocument;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides access to the displays source code through HTTP.
 *  
 * @author  Andrey Petrov
 * @version $Revision: 1.4 $
 */
public class RepositoryServlet extends HttpServlet {
    
    private static final Pattern PATH_PATTERN = Pattern.compile(
        "^(/[^\\$\\.]*)(?:\\$(\\d+))?(?:\\.(\\w+))?$"
    );
    
    private static final Logger log = Logger.getLogger( RepositoryServlet.class.getName());

    private final RepositoryAccess repo = new FileRepositoryAccess();
    
    public RepositoryServlet() {}
    
    @Override
    public void init( ServletConfig conf ) throws ServletException {
        super.init( conf );
        log.info( "Initialized " + this );
    }
    
    @Override
    public void destroy() {
        super.destroy();
        log.info( "Destroyed " + this );
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) 
            throws ServletException, IOException {
        
        String path = null;
        
        try {
        
            String arg = req.getPathInfo();
            Matcher m = PATH_PATTERN.matcher( (arg == null) ? "/" : arg );
            if (!m.matches()) {
                res.sendError( 400, "Bad Request" );
                return;
            }

            path = m.group( 1 );
            long since = req.getDateHeader( "If-Modified-Since" );
            
            TimedDocument doc = repo.load( path, since );

            if (doc == null) {
                log.fine( "Not Modified: '" + path + "'" );
                res.setStatus( 304 );
                return;
            }
                
            res.setContentType( "text/xml" );
            res.setDateHeader( "Last-Modified", doc.getTime());
            res.setDateHeader( "Expires", System.currentTimeMillis());

            OutputStream out = res.getOutputStream();
            try {
                HTTPUtil.output( doc.getDocument(), out );
                log.fine( "Retrieved: '" + path + "'" );
            } finally {
                out.close();
            }
                    
        } catch (FileNotFoundException ex) {
            log.fine( "Not Found: " + path );
            res.sendError( 404, "Not Found: " + path );
        }
        
    }

}
