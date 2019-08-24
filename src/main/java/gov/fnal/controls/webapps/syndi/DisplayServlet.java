// (c) 2001-2010 Fermi Research Alliance
// $Id: DisplayServlet.java,v 1.4 2010/09/15 16:14:05 apetrov Exp $
package gov.fnal.controls.webapps.syndi;

import gov.fnal.controls.applications.syndi.repository.FileRepositoryAccess;
import gov.fnal.controls.applications.syndi.repository.RepositoryAccess;
import gov.fnal.controls.applications.syndi.runtime.SvgDisplay;
import gov.fnal.controls.applications.syndi.runtime.TimedDocument;
import gov.fnal.controls.applications.syndi.runtime.SvgDisplayManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generates HTTP data for catalogs and displays.
 *  
 * @author  Andrey Petrov, Tim Bolshakov
 * @version $Revision: 1.4 $
 */
public class DisplayServlet extends HttpServlet {

    // request = display-path [ "$" image-index ] [ "." media-type ] [ "(" params ")" ]
    //           ^                  ^                   ^                 ^
    //           1                  2                   3                 4

    private static final Pattern REQUEST_PATTERN = Pattern.compile(
        "(.*?)(?:\\$(\\d+))?(?:\\.(\\w+))?(?:\\((.*)\\))?"
    );

    private static final Logger log = Logger.getLogger( DisplayServlet.class.getName());
    
    private static final Map<String,String> IMAGE_TYPES = new HashMap<String,String>();
    
    static {
        IMAGE_TYPES.put( "png", "image/png" );
        IMAGE_TYPES.put( "gif", "image/gif" );
    }
    
    private final Map<String,Long> clients = new HashMap<String,Long>(); // id -> last data time
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    private final AtomicInteger svgReqCount = new AtomicInteger();
    private final AtomicInteger bmpReqCount = new AtomicInteger();
    private final RepositoryAccess repo = new FileRepositoryAccess();

    private RequestDispatcher displayPage, repoPage;
    private LogDb logDb;
    private SvgDisplayManager dispMan;

    public DisplayServlet() {}

    @Override
    public void init( ServletConfig config ) throws ServletException {
        
        super.init( config );

        displayPage = getServletContext().getNamedDispatcher( "DisplayPage" );
        if (displayPage == null) {
            throw new ServletException( "DisplayPage not found" );
        }

        repoPage = getServletContext().getNamedDispatcher( "RepoPage" );
        if (repoPage == null) {
            throw new ServletException( "RepoPage not found" );
        }

        logDb = (LogDb)getServletContext().getAttribute( "log-db" );
        if (logDb == null) {
            log.config( "Logging database not available" );
        }

        dispMan = new SvgDisplayManager( repo, logDb );

        svgReqCount.set( 0 );
        bmpReqCount.set( 0 );
        
        worker.scheduleAtFixedRate(
            new Evictor(),
            SvgDisplayManager.MAX_IDLE_TIME,
            SvgDisplayManager.MAX_IDLE_TIME / 2,
            TimeUnit.MILLISECONDS 
        );
        if (logDb != null) {
            worker.scheduleAtFixedRate(
                new Reporter(), 
                1, 
                1, 
                TimeUnit.MINUTES 
            );
        }

        log.info( "Initialized " + this );

    }
    
    @Override
    public void destroy() {
        
        worker.shutdownNow();

        dispMan.dispose();
        dispMan = null;

        displayPage = null;
        repoPage = null;
        logDb = null;
        
        super.destroy();

        log.info( "Destroyed " + this );

    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void doGet( HttpServletRequest req, HttpServletResponse res ) 
            throws IOException, ServletException {

        if (res.isCommitted()) {
            log.warning( "Response is already committed" );
            res.setStatus( 500 );
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            res.sendRedirect( req.getRequestURI() + "/" );
            return;
        }
        
        Matcher m = REQUEST_PATTERN.matcher( pathInfo );
        if (!m.matches()) {
            res.sendError( 400, "Bad Request: " + pathInfo );
            return;
        }
        
        String dispName = m.group( 1 );
        String imageId = m.group( 2 );
        String mediaType = m.group( 3 );
        String params = m.group( 4 );

        if (repo.isDirectory( dispName )) {
            handleCatalogRequest( dispName, imageId, mediaType, params, req, res );
        } else if (repo.isDisplay( dispName )) {
            handleDisplayRequest( dispName, imageId, mediaType, params, req, res );
        } else {
            res.sendError( 404, "Not Found: " + dispName );
        }
        
    }


    private void handleCatalogRequest(
            String dispName,
            String imageId,
            String mediaType,
            String params,
            HttpServletRequest req,
            HttpServletResponse res ) throws IOException, ServletException {

        if (imageId != null || params != null) {

            res.sendError( 400, "Bad Request" );

        } else if (mediaType == null) {

            repoPage.forward( req, res );

        } else if ("xml".equals( mediaType )) {

            TimedDocument doc = repo.load( dispName );

            res.setContentType( "text/xml" );
            res.setDateHeader( "Last-Modified", doc.getTime());
            res.setDateHeader( "Expires", System.currentTimeMillis());

            OutputStream out = res.getOutputStream();
            try {
                HTTPUtil.output( doc.getDocument(), out );
                log.fine( "Retrieved xml repository path " + dispName );
            } finally {
                out.close();
            }

        } else {

            res.sendError( 415, "Unsupported media type: " + mediaType );

        }
    }

    private void handleDisplayRequest(
            String dispName,
            String imageId,
            String mediaType,
            String params,
            HttpServletRequest req,
            HttpServletResponse res ) throws IOException, ServletException {

        req.setAttribute( "DISPLAY_NAME", dispName );

        if (imageId != null || isImage( mediaType )) {

            handleImageRequest( dispName, imageId, mediaType, params, req, res );

        } else if (mediaType == null) {

            req.setAttribute( "DISPLAY_TITLE", dispMan.getDisplayTitle( dispName ));
            displayPage.forward( req, res );

        } else if (("xml".equals( mediaType ) || "svg".equals( mediaType ))) {

            svgReqCount.incrementAndGet();

            String clientId = req.getParameter( "cid" );
            if ("".equals( clientId )) {
                clientId = null;
            }

            TimedDocument doc;

            if ("svg".equals( mediaType )) {
                doc = dispMan.getDisplay( dispName, params ).getSvg();
                res.setContentType( "image/svg+xml" );
            } else {
                Long pastTime = null;
                if (clientId != null) {
                    synchronized (clients) {
                        pastTime = clients.get( clientId );
                    }
                }
                doc = dispMan.getDisplay( dispName, params ).getSvgDiff( pastTime );
                res.setContentType( "text/xml" );
            }

            long thisTime = doc.getTime();

            if (clientId != null) {
                synchronized (clients) {
                    if (clients.put( clientId, thisTime ) == null) {
                        log.fine( "Saving session " + clientId );
                    }
                }
            }

            res.setDateHeader( "Last-Modified", thisTime );
            res.setDateHeader( "Expires", System.currentTimeMillis());
            res.setIntHeader( "X-Update-Rate", SvgDisplay.SVG_UPDATE_RATE );

            OutputStream out = res.getOutputStream();
            try {
                HTTPUtil.output( doc.getDocument(), out );
            } finally {
                out.close();
            }

        } else {

            res.sendError( 415, "Unsupported Media Type: " + mediaType );

        }
    }

    private void handleImageRequest(
            String dispName,
            String imageId,
            String mediaType,
            String params,
            HttpServletRequest req,
            HttpServletResponse res ) throws IOException {

        bmpReqCount.incrementAndGet();

        String contentType = IMAGE_TYPES.get( mediaType );
        if (contentType != null) {

            Integer iid = (imageId != null) ? new Integer( imageId ) : null;
            BufferedImage image = dispMan.getDisplay( dispName, params ).getImage( iid );

            res.setContentType( contentType );
            res.setDateHeader( "Last-Modified", System.currentTimeMillis());
            res.setDateHeader( "Expires", System.currentTimeMillis());

            if (image != null) {
                OutputStream out = res.getOutputStream();
                try {
                    HTTPUtil.output( image, out, mediaType );
                } finally {
                    out.close();
                }
            }

        } else {

            res.sendError( 415, "Unsupported Media Type: " + mediaType );
            
        }
    }

    private boolean isImage( String mediaType ) {
        return mediaType != null && IMAGE_TYPES.containsKey( mediaType );
    }

    public int getSvgReqCountAndReset() {
        return svgReqCount.getAndSet( 0 );
    }

    public int getBmpReqCountAndReset() {
        return bmpReqCount.getAndSet( 0 );
    }

    private class Evictor implements Runnable {
        
        @Override
        public void run() {
            long t0 = System.currentTimeMillis() - SvgDisplayManager.MAX_IDLE_TIME;
            synchronized (clients) {
                for (Iterator<Entry<String,Long>> z = clients.entrySet().iterator(); z.hasNext();) {
                    Entry<String,Long> e = z.next();
                    if (e.getValue() >= t0) {
                        continue;
                    }
                    log.fine( "Removing session " + e.getKey());
                    z.remove();
                }
            }
        }
        
    }
    
    private class Reporter implements Runnable {
        
        private final MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        
        @Override
        public void run() {
            log.finer( "Reporting statistics" );
            int heapSize = (int)(memBean.getHeapMemoryUsage().getUsed() / 1024);
            int svgReqCount = getSvgReqCountAndReset();
            int bmpReqCount = getBmpReqCountAndReset();
            int displayCount = dispMan.getDisplayCount();
            int startCount = dispMan.getStartCountAndReset();
            int stopCount = dispMan.getStopCountAndReset();
            int abortCount = dispMan.getAbortCountAndReset();
            if (logDb != null) {
                logDb.stat(
                    heapSize,
                    svgReqCount, 
                    bmpReqCount, 
                    displayCount, 
                    startCount, 
                    stopCount, 
                    abortCount
                );
            }
        }
        
    }

}
