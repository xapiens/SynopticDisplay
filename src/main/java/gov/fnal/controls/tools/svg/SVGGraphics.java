// (c) 2010 Fermi Research Alliance
// $Id: SVGGraphics.java,v 1.7 2010/09/16 16:08:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.net.URI;
import java.text.AttributedCharacterIterator;
import java.util.Formatter;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Graphics2D object producing SVG XML when components get painted on it. 
 * 
 * @author Timofei Bolshakov, Andrey Petrov
 * @version $Revision: 1.7 $
 */

public class SVGGraphics extends Graphics2D implements Cloneable, SVGNamespace {
    
    private final FontRenderContext fontRenderContext = new FontRenderContext(
        null, 
        RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, 
        RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT
    );
    private final RenderingHints hints = new RenderingHints( null );
    private final SVGDOM dom;
    private final Map<Integer,BufferedImage> images;
    private final Component comp;
    private final Rectangle bounds;
    private final String lastPathElement, pathBack, params;

    protected String svgBgColor, svgFgColor;
    protected AffineTransform xform = new AffineTransform();
    protected Stroke stroke;
    protected Shape clip;
    protected String toolTip;
    protected SVGAnchor link, absoluteLink;
    protected Font font;
    protected Color bgColor, fgColor;

    private static String getLastPathElement( String pathInfo ) {
        if (pathInfo == null) {
            return "";
        }
        int i = pathInfo.lastIndexOf( "/" );
        if (i == -1) {
            return pathInfo;
        } else if (i == pathInfo.length() - 1) {
            return "";
        } else {
            return pathInfo.substring( i + 1 );
        }
    }

    private static String getPathBack( String pathInfo ) {
        if (pathInfo == null) {
            return "";
        }
        int n = 0;
        for (int i = pathInfo.length() - 1; i >= 0; i--) {
            if (pathInfo.charAt( i ) == '/') {
                n++;
            }
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < n - 1; i++) {
            if (buf.length() > 0) {
                buf.append( "/" );
            }
            buf.append( ".." );
        }
        return buf.toString();
    }

    public SVGGraphics( Component comp, String pathInfo, Map<Integer,BufferedImage> images ) {
        this( comp, pathInfo, null, images );
    }

    public SVGGraphics( Component comp, String pathInfo, String params, Map<Integer,BufferedImage> images ) {

        if (comp == null) {
            throw new NullPointerException();
        }

        this.comp = comp;
        this.images = images;
        this.params = params;

        bounds = comp.getBounds();
        dom = new SVGDOM( bounds );
        lastPathElement = getLastPathElement( pathInfo );
        pathBack = getPathBack( pathInfo );

        setColor( SVGComponent.DEFAULT_STROKE_COLOR );
        setBackground( SVGComponent.DEFAULT_FILL_COLOR );
        setStroke( SVGComponent.DEFAULT_STROKE );
        setFont( SVGComponent.DEFAULT_FONT );

    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SVGGraphics res = (SVGGraphics)super.clone(); // shallow
        res.xform = (AffineTransform)this.xform.clone();
        return res;
    }
    
    public Document getDocument() {
        return dom.getDocument();
    }

    private Element createElement( String tag ) {
        return dom.createElement( tag, this );
    }
    
//  __________ L I F E C Y C L E ____________________________________
    
    @Override
    public Graphics create() {
        try {
            return (Graphics)clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException( ex );
        }
    }
    
    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        return null; // not supported
    }
    
    @Override
    public void dispose() {
    }
    
//  __________ A T T R I B U T E S __________________________________
    
    @Override
    public void setFont( Font font ) {
        this.font = font;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public FontMetrics getFontMetrics( Font font ) {
        return comp.getFontMetrics( font );
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    @Override
    public void setColor( Color color ) {
        fgColor = color;
        svgFgColor = SVGColor.toString( color );
    }
    
    @Override
    public Color getColor() {
        return fgColor;
    }

    @Override
    public void setBackground( Color color ) {
        bgColor = color;
        svgBgColor = SVGColor.toString( color );
    }
    
    @Override
    public Color getBackground() {
        return bgColor;
    }
    
    @Override
    public void setPaint( Paint paint ) {
        if (paint != null && paint instanceof Color) {
            setColor( (Color)paint );
        }
    }
    
    @Override
    public Paint getPaint() {
        return fgColor;
    }
    
    @Override
    public void setComposite( Composite comp ) {
        // not supported
    }
    
    @Override
    public Composite getComposite() {
        return null; // not supported
    }
    
    @Override
    public void setPaintMode() {
        // not supported
    }
    
    @Override
    public void setXORMode( Color color ) {
        // not supported
    }
    
    @Override
    public void setStroke( Stroke stroke ) {
        this.stroke = stroke;
    }
    
    @Override
    public Stroke getStroke() {
        return stroke;
    }
    
//  __________ C L I P P I N G ______________________________________
    
    @Override
    public void setClip( int x, int y, int w, int h ) {
        setClip( new Rectangle( x, y, w, h ));
    }

    @Override
    public void setClip( Shape clip ) {
        this.clip = bounds.equals( clip ) ? null : clip;
    }
    
    @Override
    public Shape getClip() {
        return clip;
    }

    @Override
    public Rectangle getClipBounds() {
        return (clip == null) ? new Rectangle( bounds ) : clip.getBounds();
    }
    
    @Override
    public void clipRect( int x, int y, int w, int h ) {
        clip( new Rectangle( x, y, w, h ));
    }

    @Override
    public void clip( Shape clip ) {
        if (this.clip == null || clip == null) {
            setClip( clip );
        } else {
            setClip( this.clip.getBounds2D().createIntersection( clip.getBounds2D()));
        }
    }
    
    @Override
    public void copyArea( int x, int y, int w, int h, int dx, int dy ) {
        // not supported
    }
    
    @Override
    public boolean hit( Rectangle rect, Shape s, boolean onStroke ) {
        return rect.intersects( s.getBounds());
    }
    
//  __________ T R A N F O R M A T I O N ___________________________
    
    @Override
    public AffineTransform getTransform() {
        return (AffineTransform)xform.clone();
    }

    @Override
    public void setTransform( AffineTransform xform ) {
        if (xform == null) {
            xform = new AffineTransform();
        }
        if (!xform.equals( this.xform )) {
            this.xform = (AffineTransform)xform.clone();
        }
    }
    
    @Override
    public void transform( AffineTransform xform ) {
        if (xform != null) {
            this.xform.concatenate( xform );
        }
    }
    
    @Override
    public void translate( int x, int y ) {
        translate( (double)x, (double)y );
    }

    @Override
    public void translate( double x, double y ) {
        xform.translate( x, y );
    }
    
    @Override
     public void shear( double x, double y ) {
        xform.shear( x, y );
    }
    
    @Override
    public void rotate( double theta ) {
        xform.rotate( theta );
    }
    
    @Override
    public void rotate( double theta, double x, double y ) {
        xform.rotate( theta, x, y );
    }
    
    @Override
    public void scale( double x, double y ) {
        xform.scale( x, y );
    }
    
//  __________ R E N D E R I N G   H I N T S ________________________
    
    @Override
    public void addRenderingHints( Map<?,?> hints ) {
        this.hints.putAll( hints );
    }
    
    @Override
    public void setRenderingHint( RenderingHints.Key hintKey, Object hint ) {
        hints.put( hintKey, hint );
    }
    
    @Override
    public void setRenderingHints( Map<?,?> hints ) {
        this.hints.clear();
        this.hints.putAll( hints );
    }
    
    @Override
    public RenderingHints getRenderingHints() {
        return hints;
    }
    
    @Override
    public Object getRenderingHint( RenderingHints.Key hintKey ) {
        return hints.get( hintKey );
    }
    
//  __________ L I N E ______________________________________________
    
    @Override
    public void drawLine( int x1, int y1, int x2, int y2 ) {
        Element e = createElement( "path" );
        e.setAttribute( "d", String.format( "M%d %dL%d %d", x1, y1, x2, y2 ));
        e.setAttribute( "stroke", svgFgColor );
    }
    
    @Override
    public void drawPolyline( int[] xx, int[] yy, int len ) {
        Formatter fmt = new Formatter( new StringBuilder());
        for (int i = 0; i < len; i++) {
            fmt.format( "%d,%d ", xx[ i ], yy[ i ]);
        }
        Element e = createElement( "polyline" );
        e.setAttribute( "points", fmt.toString().trim());
        e.setAttribute( "stroke", svgFgColor );
    }
    
//  __________ S H A P E ____________________________________________

    private Element newShapeElement( Shape shape ) {
        Element e = createElement( "path" );
        e.setAttribute( "d", SVGPath.toString( shape ));
        return e;
    }
    
    @Override
    public void draw( Shape shape ) {
        Element e = newShapeElement( shape );
        e.setAttribute( "stroke", svgFgColor );
    }
    
    @Override
    public void fill( Shape shape ) {
        Element e = newShapeElement( shape );
        e.setAttribute( "fill", svgFgColor );
    }
    
//  __________ R E C T A N G L E ____________________________________
    
    private Element newRectElement( int x, int y, int w, int h, int rx, int ry ) {
        Element e = createElement( "rect" );
        e.setAttribute( "x", String.valueOf( x ));
        e.setAttribute( "y", String.valueOf( y ));
        e.setAttribute( "width", String.valueOf( w ));
        e.setAttribute( "height", String.valueOf( h ));
        if (rx != 0) {
            e.setAttribute( "rx", String.valueOf( rx ));
        }
        if (ry != 0) {
            e.setAttribute( "ry", String.valueOf( ry ));
        }
        return e;
    }
    
    @Override
    public void drawRect( int x, int y, int w, int h ) {
        Element e = newRectElement( x, y, w, h, 0, 0 );
        e.setAttribute( "stroke", svgFgColor );
    }
    
    @Override
    public void drawRoundRect( int x, int y, int w, int h, int rx, int ry ) {
        Element e = newRectElement( x, y, w, h, rx, ry );
        e.setAttribute( "stroke", svgFgColor );
    }

    
    @Override
    public void fillRect( int x, int y, int w, int h ) {
        Element e = newRectElement( x, y, w, h, 0, 0 );
        e.setAttribute( "fill", svgFgColor );
    }
    
    @Override
    public void fillRoundRect( int x, int y, int w, int h, int rx, int ry ) {
        Element e = newRectElement( x, y, w, h, rx, ry );
        e.setAttribute( "fill", svgFgColor );
    }
    
    @Override
    public void clearRect( int x, int y, int w, int h ) {
        Element e = newRectElement( x, y, w, h, 0, 0 );
        e.setAttribute( "fill", svgBgColor );
    }
    
//  __________ O V A L S ______________________________________

    private Element newOvalElement( int x, int y, int w, int h ) {
        double dw = 0.5 * w;
        double dh = 0.5 * h;
        Element e = createElement( "ellipse" );
        e.setAttribute( "cx", SVGNumber.toString( dw + x ));
        e.setAttribute( "cy", SVGNumber.toString( dh + y ));
        e.setAttribute( "rx", SVGNumber.toString( dw ));
        e.setAttribute( "ry", SVGNumber.toString( dh ));
        return e;
    }
    
    @Override
    public void drawOval( int x, int y, int w, int h ) {
        Element e = newOvalElement( x, y, w, h );
        e.setAttribute( "stroke", svgFgColor );
    }
    
    @Override
    public void fillOval( int x, int y, int w, int h ) {
        Element e = newOvalElement( x, y, w, h );
        e.setAttribute( "fill", svgFgColor );
    }
    
//  __________ A R C ______________________________________
    
    private Element newArcElement( int x, int y, int w, int h, int startAngle, int arcAngle, boolean close ) {
        
        int delta = arcAngle;
        arcAngle += startAngle;
        startAngle %= 360;
        if (startAngle > 180) {
            startAngle -= 360;
        }
        arcAngle %= 360;
        if (arcAngle > 180) {
            arcAngle -= 360;
        }

        double alpha = -Math.toRadians( (double)startAngle );
        double beta = -Math.toRadians( (double)arcAngle );

        double xc = x + w * 0.5;
        double yc = y + h * 0.5;
        double x1 = xc + w * Math.cos( alpha ) * 0.5;
        double y1 = yc + h * Math.sin( alpha ) * 0.5;
        double x2 = xc + w * Math.cos( beta ) * 0.5;
        double y2 = yc + h * Math.sin( beta ) * 0.5;
        
        Formatter fmt = new Formatter( new StringBuilder());
        if (close) {
            fmt.format( "M %.1f %.1f ", xc, yc );
        }
        fmt.format( "%c %.1f %.1f A %.1f %.1f 0 %c %c %.1f %.1f ",
            close ? 'L' : 'M', 
            x1, 
            y1, 
            w * 0.5, 
            h * 0.5, 
            Math.abs( delta ) >= 180 ? '1' : '0',
            delta == 180 
                ? ((startAngle != 0 && startAngle != 180) ? '1' : '0') 
                : ((arcAngle > startAngle) ? '0' : '1'), 
            x2, 
            y2
        );
        if (close) {
            fmt.format( "Z" );
        }
        
        Element e = createElement( "path" );
        e.setAttribute( "d", fmt.toString());
        return e;
        
    }
    
    @Override
    public void drawArc( int x, int y, int w, int h, int sa, int aa ) {
        Element e = newArcElement( x, y, w, h, sa, aa, false );
        e.setAttribute( "stroke", svgFgColor );
    }
    
    @Override
    public void fillArc( int x, int y, int w, int h, int sa, int aa ) {
        Element e = newArcElement( x, y, w, h, sa, aa, true );
        e.setAttribute( "fill", svgFgColor );
    }
    
//  __________ P O L Y G O N ______________________________________
    
    private Element newPolygonElement( int[] xx, int[] yy, int len ) {
        Formatter fmt = new Formatter( new StringBuilder());
        for (int i = 0; i < len; i++) {
            fmt.format( "%d,%d ", xx[ i ], yy[ i ]);
        }
        Element e = createElement( "polygon" );
        e.setAttribute( "points", fmt.toString());
        return e;
    }
    
    @Override
    public void drawPolygon( int[] xx, int[] yy, int len ) {
        Element e = newPolygonElement( xx, yy, len );
        e.setAttribute( "stroke", svgFgColor );
    }
    
    @Override
    public void fillPolygon( int[] xx, int[] yy, int len ) {
        Element e = newPolygonElement( xx, yy, len );
        e.setAttribute( "fill", svgFgColor );
    }
    
//  __________ S T R I N G __________________________________________
    
    private Element newStringElement( String str, float x, float y ) {
        Element e = createElement( "text" );
        if (str != null) {
            e.setTextContent( str );
        }
        e.setAttribute( "fill", svgFgColor );
        e.setAttribute( "x", SVGNumber.toString( x ));
        e.setAttribute( "y", SVGNumber.toString( y ));
        if (font != null) {
            e.setAttribute( "font-size", SVGNumber.toString( font.getSize()));
            if (font.isItalic()) {
                e.setAttribute( "font-style", "italic" );
            }
            if (font.isBold()) {
                e.setAttribute( "font-weight", "bold" );
            }
        }
        return e;
    }
    
    @Override
    public void drawString( String str, int x, int y ) {
        newStringElement( str, x, y );
    }
    
    @Override
    public void drawString( String str, float x, float y ) {
        newStringElement( str, x, y );
    }
    
    @Override
    public void drawString( AttributedCharacterIterator iterator, int x, int y ) {
        // not supported
    }
    
    @Override
    public void drawString( AttributedCharacterIterator iterator, float x, float y ) {
        // not supported
    }
    
    @Override
    public void drawGlyphVector( GlyphVector gv, float x, float y ) {
        Shape shape = gv.getOutline( x, y );
        Element e = newShapeElement( shape );
        e.setAttribute( "stroke", "none" );
        e.setAttribute( "stroke-width", "0" );
        e.setAttribute( "fill", svgFgColor );
    }

//  __________ I M A G E ____________________________________________
    
    private Element newImageElement( BufferedImage img, int x, int y, int w, int h ) {
        int imageId = dom.nextId();
        if (images != null) {
            images.put( imageId, img );
        }
        Element e = createElement( "image" );
        e.setAttribute( "x", String.valueOf( x ));
        e.setAttribute( "y", String.valueOf( y ));
        e.setAttribute( "width", String.valueOf( w ));
        e.setAttribute( "height", String.valueOf( h ));
        e.setAttribute( "fill", svgFgColor );
        String imageUrl = lastPathElement + "$" + imageId + ".png";
        if (params != null) {
            imageUrl += "(" + params + ")";
        }
        e.setAttributeNS( XMLNS_XLINK, "xlink:href", imageUrl );
        return e;
    }
    
    @Override
    public void drawRenderableImage( RenderableImage img, AffineTransform xform ) {
        drawRenderedImage( img.createDefaultRendering(), xform );
    }
    
    @Override
    public void drawRenderedImage( RenderedImage img, AffineTransform xform ) {
        if (xform == null) {
            xform = new AffineTransform();
        }
        int x = (int)xform.getTranslateX();
        int y = (int)xform.getTranslateY();
        int w = (int)Math.ceil( img.getWidth() * xform.getScaleX());
        int h = (int)Math.ceil( img.getHeight() * xform.getScaleY());
        BufferedImage buf = null;
        if (images != null) {
            buf = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g2d = buf.createGraphics();
            try {
                g2d.drawRenderedImage( img, xform );
                buf.flush();
            } finally {
                g2d.dispose();
            }
        }
        newImageElement( buf, x, y, w, h ); 
    }
    
    @Override
    public boolean drawImage( Image img, AffineTransform xform, ImageObserver obs ) {
        if (xform == null) {
            xform = new AffineTransform();
        }
        int x = (int)xform.getTranslateX();
        int y = (int)xform.getTranslateY();
        int w = (int)Math.ceil( img.getWidth( obs ) * xform.getScaleX());
        int h = (int)Math.ceil( img.getHeight( obs ) * xform.getScaleY());
        return drawImage( img, x, y, w, h, null, obs );
    }
    
    @Override
    public boolean drawImage( Image img, int x, int y, ImageObserver obs ) {
        return drawImage( img, x, y, img.getWidth( obs ), img.getHeight( obs ), null, obs );
    }
    
    @Override
    public boolean drawImage( Image img, int x, int y, Color bgcolor, ImageObserver obs ) {
        return drawImage( img, x, y, img.getWidth( obs ), img.getHeight( obs ), bgcolor, obs );
    }
    
    @Override
    public boolean drawImage( Image img, int x, int y, int w, int h, ImageObserver obs ) {
        return drawImage( img, x, y, w, h, null, obs );
    }
    
    @Override
    public boolean drawImage( Image img, int x, int y, int w, int h, Color bgcolor, ImageObserver obs ) {
        BufferedImage buf = null;
        if (images != null) {
            buf = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g2d = buf.createGraphics();
            try {
                if (bgcolor != null) {
                    g2d.setColor( bgcolor );
                    g2d.fillRect( 0, 0, w, h );
                }
                g2d.drawImage( img, 0, 0, w, h, obs );
                buf.flush();
            } finally {
                g2d.dispose();
            }
        }
        newImageElement( buf, x, y, w, h );
        return true;
    }
    
    @Override
    public boolean drawImage(
            Image img, 
            int dx1, int dy1, int dx2, int dy2, 
            int sx1, int sy1, int sx2, int sy2, 
            ImageObserver obs ) {
        return drawImage( img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, obs );
    }
    
    @Override
    public boolean drawImage( 
            Image img, 
            int dx1, int dy1, int dx2, int dy2, 
            int sx1, int sy1, int sx2, int sy2, 
            Color bgcolor, ImageObserver obs ) {  
        AffineTransform xform_ = new AffineTransform(
            (double)(dx2 - dx1) / (double)(sx2 - sx1), 
            0.0,
            0.0, 
            (double)(dy2 - dy1) / (double)(sy2 - sy1),
            (double)sx1 / (double)(dx1 - dx2), 
            (double)sy1 / (double)(dy1 - dy2)
        );
        return drawImage( img, xform_, obs );
    }

    @Override
    public void drawImage( BufferedImage img, BufferedImageOp op, int x, int y ) {
        BufferedImage img1 = op.createCompatibleDestImage( img, DirectColorModel.getRGBdefault());
        op.filter( img, img1 );
        drawImage( img1, x, y, null );
    }

//  __________ T O O L   T I P ____________________________________________

    public void setToolTip( String toolTip ) {
        this.toolTip = toolTip;
    }

    public String getToolTip() {
        return toolTip;
    }

//  __________ L I N K ____________________________________________

    public void setLink( SVGAnchor link ) {
        absoluteLink = link == null ? null : createAbsoluteLink( link );
        this.link = link;
    }

    public SVGAnchor getLink() {
        return link;
    }

    public void markHover() {
        dom.markHover();
    }

    // TODO: Optimize strings
    private SVGAnchor createAbsoluteLink( SVGAnchor link ) {
        String uri = link.getURI().toString();
        if (uri.toLowerCase().startsWith( "repo:" )) {
            uri = uri.substring( 5 );
        }
        if (!uri.startsWith( "/" )) {
            return link;
        }
        while (uri.startsWith( "/" )) {
            uri = uri.substring( 1 );
        }
        if (!pathBack.isEmpty()) {
            uri = pathBack + "/" + uri;
        }
        URI absoluteURI = URI.create( uri );
        return new SVGAnchor( absoluteURI, link.getTarget(), link.getTitle());
    }

}
