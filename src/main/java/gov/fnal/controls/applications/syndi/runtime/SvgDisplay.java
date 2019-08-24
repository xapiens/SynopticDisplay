// (c) 2001-2010 Fermi Research Allaince
// $Id: SvgDisplay.java,v 1.4 2010/09/15 18:43:05 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.repository.DisplayParameters;
import gov.fnal.controls.tools.svg.SVGGraphics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Runtime wrapper around a synoptic display, used by <code>DisplayServlet</code> 
 * to extract graphical data.
 * 
 * @author  Andrey Petrov
 * @version $Revision: 1.4 $
 */
public class SvgDisplay {
    
    public static final int SVG_UPDATE_RATE =
            Integer.getInteger( "Synoptic.svg-update-rate", 5000 );

    public static final int BMP_UPDATE_RATE =
            Integer.getInteger( "Synoptic.bmp-update-rate", 60000 );

    public static final int SVG_CACHE_SIZE =
            Integer.getInteger( "Synoptic.svg-cache-size", 4 );

    private static final SvgDisplayImages STANDARD_IMAGES = new SvgDisplayImages();

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    private static final Logger log = Logger.getLogger( SvgDisplay.class.getName());

    static {

        dbf.setValidating( false );
        dbf.setNamespaceAware( true );

        if (SVG_UPDATE_RATE < 500) {
            throw new Error( "Illegal SVG update rate: " + SVG_UPDATE_RATE );
        }
        if (BMP_UPDATE_RATE < 1000) {
            throw new Error( "Illegal BMP update rate: " + BMP_UPDATE_RATE );
        }
        if (SVG_CACHE_SIZE < 1) {
            throw new Error( "Illegal SVG cache size: " + SVG_CACHE_SIZE );
        }

        StringBuilder buf = new StringBuilder( "SVG Display Properties:" );
        buf.append( "\n    SVG_UPDATE_RATE: " );
        buf.append( SVG_UPDATE_RATE );
        buf.append( "\n    BMP_UPDATE_RATE: " );
        buf.append( BMP_UPDATE_RATE );
        buf.append( "\n     SVG_CACHE_SIZE: " );
        buf.append( SVG_CACHE_SIZE );
        log.config( buf.toString());

    }

    private final String name, params;
    private final SvgCache svgCache;
    private final SvgGrabber svgGrabber;
    private final Timer timer;
    private final DocumentBuilder builder;

    private HeadlessFrame frame;
    private long accessTime = System.currentTimeMillis();
    private long version = -1;
    private Document svg;
    private boolean svgStored;
    private Map<Integer,BufferedImage> images;
    private long lastSvgUpdate, lastBitmapUpdate;
    private boolean disposed;
    
    SvgDisplay( String name, String params ) {
        this.name = name;
        this.params = params;
        this.svgCache = new SvgCache( SVG_CACHE_SIZE );
        this.svgGrabber = new SvgGrabber();
        this.timer = new Timer( SVG_UPDATE_RATE, svgGrabber );
        try {
            builder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new Error( ex );
        }
        showBanner( STANDARD_IMAGES.getStartBanner());
    }
    
    public String getName() {
        return name;
    }

    public String getParameters() {
        return params;
    }
    
    public long getVersion() {
        return version;
    }
    
    public boolean isRunning() {
        return (frame != null);
    }
    
    public long getIdleTime() {
        return System.currentTimeMillis() - accessTime;
    }
    
    public TimedDocument getSvg() {
        accessTime = System.currentTimeMillis();
        synchronized (svgCache) {
            if (!svgStored) {
                svgCache.put( lastSvgUpdate, svg );
                svgStored = true;
            }
            Document doc = (Document)svg.cloneNode( true );
            return new TimedDocument( doc, lastSvgUpdate );
        }
    }
    
    public TimedDocument getSvgDiff( Long pastTime ) {
        accessTime = System.currentTimeMillis();
        synchronized (svgCache) {
            if (!svgStored) {
                svgCache.put( lastSvgUpdate, svg );
                svgStored = true;
            }
            Document svg0 = svgCache.get( pastTime );
            if (svg0 != null) {
                Document dif = getDOMDiff( svg0, svg );
                if (dif != null) {
                    return new TimedDocument( dif, lastSvgUpdate );
                }
            }
            Document doc = (Document)svg.cloneNode( true );
            log.finer( "SVG difference not available" );
            return new TimedDocument( doc, lastSvgUpdate );
        }
    }
    
    public BufferedImage getImage( Integer id ) {
        accessTime = System.currentTimeMillis();
        if (id == null) {
            BitmapGrabber grabber = new BitmapGrabber();
            try {
                SwingUtilities.invokeAndWait( grabber );
            } catch (Exception ex) {
                log.throwing( getClass().getName(), "getBitmap", ex );
            }
            return (grabber.image != null) ? grabber.image : STANDARD_IMAGES.getDefaultImage();
        } else {
            synchronized (svgCache) {
                BufferedImage img = null;
                if (images != null) {
                    img = images.get( id );
                }
                return (img != null) ? img : STANDARD_IMAGES.getDefaultImage();
            }
        }
    }

    public synchronized void start( TimedDocument doc ) throws IllegalStateException {
        if (doc == null) {
            throw new NullPointerException();
        }
        if (disposed) {
            throw new IllegalStateException( "Display is disposed" );
        }
        if (isRunning()) {
            throw new IllegalStateException( "Display is already running" );
        }
        try {
            Document xmlDoc = doc.getDocument();
            DisplayParameters paramMap = (params != null)
                    ? DisplayParameters.parse( params )
                    : null;
            frame = new HeadlessFrame( xmlDoc, paramMap );
            version = doc.getTime();
            timer.start();
            log.info( "Started " + this );
        } catch (Exception ex) {
            showBanner( STANDARD_IMAGES.getErrorBanner( ex.getMessage()));
            log.log( Level.SEVERE, "Cannot start display " + name, ex );
        }
    }
    
    public void stop() {
        stop( null );
    }
    
    public synchronized void stop( String reason ) {
        if (isRunning()) {
            timer.stop();
            try {
                frame.setVisible( false );
                frame.dispose();
            } catch (Throwable ex) {
                log.throwing( getClass().getName(), "dispose", ex );
            } finally {
                frame = null;
                version = -1;
            }
            log.info( "Stopped " + this );
        }
        showBanner( STANDARD_IMAGES.getErrorBanner( reason == null ? "Aborted" : reason ));
    }
    
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        stop();
        timer.removeActionListener( svgGrabber );
        images = null;
    }
    
    public boolean isDisposed() {
        return disposed;
    }
    
    private void showBanner( Document banner ) {
        if (banner == null) {
            return;
        }
        synchronized (svgCache) {
            svg = banner;
            svgStored = false;
            lastSvgUpdate = System.currentTimeMillis();
        }
    }
    
    private RootComponent getCanvas() {
        HeadlessFrame f = this.frame;
        if (f == null) {
            return null;
        } else {
            return f.getCanvas();
        }
    }
    
    @Override
    public String toString() {
        return "Display[name=" + name + ";params=(" + params + ")]";
    }

    private Document getDOMDiff( Document d0, Document d1 ) {
        Document difDoc = builder.newDocument();
        difDoc.appendChild( difDoc.createElement( "dif" ));
        boolean difReady = getDOMDiff(
            difDoc,
            d0.getDocumentElement(),
            d1.getDocumentElement()
        );
        return difReady? difDoc : null;
    }
    
    private boolean getDOMDiff( Document doc, Element e0, Element e1 ) {

        String tag0 = e0.getTagName();
        String tag1 = e1.getTagName();

        if (!tag0.equals( tag1 )) {
            //log.finer( "Tag mismatch: " + tag0 + "->" + tag1 );
            //return ASK;
            return false;
        }
        
        String id = e0.getAttribute( "id" );
        if (id == null) {
            //return doc; // Don't compare elements without ID
            return true;
        }
        
        NamedNodeMap att0 = e0.getAttributes();
        NamedNodeMap att1 = e1.getAttributes();
        
        if (att0.getLength() != att1.getLength()) {
            //log.finer( "Attribute count mismatch @" + id );
            //return ASK;
            return false;
        }
        
        for (int i = 0, n = att0.getLength(); i < n; i++) {
            
            Attr a0 = (Attr)att0.item( i );
            String name0 = a0.getName();
            
            Attr a1 = (Attr)att1.getNamedItem( name0 );
            if (a1 == null) {
                //log.finer( "Attribute " + name0 + " missing @" + id );
                //return ASK;
                return false;
            }
            String value1 = a1.getValue();
            if (!value1.equals( a0.getValue())) {
                if ("id".equals( name )) {
                    //log.finer( "Element ID mismatch @" + id );
                    //return ASK;
                    return false;
                }
                Element e = doc.createElement( "attribute" );
                e.setAttribute( "id", id );
                e.setAttribute( "name", name0 );
                e.setAttribute( "value", value1 );
                doc.getDocumentElement().appendChild( e );
            }

        }
        
        String text1 = getText( e1 );
        if (!text1.equals( getText( e0 ))) {
            Element e = doc.createElement( "text" );
            e.setAttribute( "id", id );
            e.setTextContent( text1 );
            doc.getDocumentElement().appendChild( e );
        }
        
        NodeList cld0 = e0.getChildNodes();
        NodeList cld1 = e1.getChildNodes();

        if (cld0.getLength() != cld1.getLength()) {
            //log.finer( "Child count mismatch @" + id );
            //return ASK;
            return false;
        }

        for (int i = 0, n = cld0.getLength(); i < n; i++) {
            
            Node n0 = cld0.item( i );
            short type0 = n0.getNodeType();
            
            Node n1 = cld1.item( i );

            if (type0 != n1.getNodeType()) {
                //log.finer( "Type mismatch" );
                //return ASK;
                return false;
            }
            
            if (type0 == Node.ELEMENT_NODE 
                    && !getDOMDiff( doc, (Element)n0, (Element)n1 )) {
                //return ASK;
                return false;
            }
            
        }
        
        //return doc;
        return true;
        
    }
    
    private String getText( Element e ) {
        StringBuilder buf = new StringBuilder();
        NodeList cld = e.getChildNodes();
        for (int i = 0, n = cld.getLength(); i < n; i++) {
            Node c = cld.item( i );
            if (c.getNodeType() == Node.TEXT_NODE) {
                buf.append( c.getNodeValue());
            }
        }
        return buf.toString();
    }
    
    private class SvgCache extends LinkedHashMap<Long,Document> {
        
        private int cacheSize;
        
        private SvgCache( int size ) {
            super( size );
            this.cacheSize = size;
        }

        @Override
        protected boolean removeEldestEntry( Map.Entry eldest ) {
            return size() > cacheSize;
        }

    }
    
    private class SvgGrabber implements ActionListener {
        
        @Override
        public void actionPerformed( ActionEvent e ) {
            
            RootComponent canvas = getCanvas();
            if (canvas == null) {
                return;
            }

            Map<Integer,BufferedImage> images = null;

            long now = System.currentTimeMillis();
            lastSvgUpdate = now;
            
            if ((now - lastBitmapUpdate) >= BMP_UPDATE_RATE) {
                images = new HashMap<Integer,BufferedImage>();
                lastBitmapUpdate = now;
            }
            
            SVGGraphics g = new SVGGraphics( canvas, name, params, images );
            canvas.paint( g );
            
            synchronized (svgCache) {
                svg = g.getDocument();
                svgStored = false;
                if (images != null) {
                    SvgDisplay.this.images = images;
                }
            }
            
        }

    }
    
    private class BitmapGrabber implements Runnable {
        
        BufferedImage image = null;
        
        @Override
        public void run() {
            
            RootComponent canvas = getCanvas();
            if (canvas == null) {
                return;
            }

            image = new BufferedImage( 
                canvas.getWidth(), 
                canvas.getHeight(), 
                BufferedImage.TYPE_INT_ARGB
            );
            Graphics g = image.getGraphics();
            try {
                canvas.paint( g );
            } finally {
                g.dispose();
            }
        }
        
    }
    
}
