//  (c) 2010 Fermi Research Alliance
//  $Id: SVGComponent.java,v 1.4 2010/02/12 21:03:20 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

/**
 * A base visual SVG component.
 *
 * @author Andrey Petrov
 */

public abstract class SVGComponent implements SVGElement, SVGNamespace {

    static final Color DEFAULT_FILL_COLOR = null;
    static final Color DEFAULT_STROKE_COLOR = Color.BLACK;
    static final BasicStroke DEFAULT_STROKE = new BasicStroke();
    static final Font DEFAULT_FONT = new Font( null, Font.PLAIN, 14 );

    private static final Logger log = Logger.getLogger( SVGComponent.class.getName());

    private SVGContainer parent;
    private AffineTransform transform;
    private String title, desc;
    private Color fillColor, strokeColor;
    private Number strokeWidth, fontSize;
    private String fontFamily;
    private SVGFontStyle fontStyle;
    private SVGFontWeight fontWeight;

    private final String tag;

    protected SVGComponent() {
        SVGTag anno = getClass().getAnnotation( SVGTag.class );
        if (anno == null) {
            throw new RuntimeException( "No tag annotation" );
        }
        tag = anno.value();
    }

    @Override
    public boolean add( SVGElement comp ) {
        if (comp instanceof SVGTitle) {
            setTitle( ((SVGTitle)comp).getText());
            return true;
        }
        if (comp instanceof SVGDescription) {
            setDescription( ((SVGDescription)comp).getText());
            return true;
        }
        return false;
    }

    @Override
    public void applyAttributes( Attributes attr ) {

        String fillColor_ = attr.getValue( "fill" );
        if (fillColor_ != null) {
            try {
                setFillColor( SVGColor.parseColor( fillColor_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'fill' attribute: " + fillColor_ );
            }
        }

        String strokeColor_ = attr.getValue( "stroke" );
        if (strokeColor_ != null) {
            try {
                setStrokeColor( SVGColor.parseColor( strokeColor_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'stroke' attribute: " + strokeColor_ );
            }
        }

        String strokeWidth_ = attr.getValue( "stroke-width" );
        if (strokeWidth_ != null) {
            try {
                setStrokeWidth( SVGNumber.parseNumber( strokeWidth_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'stroke-width' attribute: " + strokeWidth_ );
            }
        }
        
        String transform_ = attr.getValue( "transform" );
        if (transform_ != null) {
            try {
                setTransform( SVGTransform.parseTransform( transform_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'transform' attribute: " + transform_ );
            }
        }

        String fontSize_ = attr.getValue( "font-size" );
        if (fontSize_ != null) {
            try {
                setFontSize( SVGNumber.parseNumber( fontSize_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Invalid 'font-size' attribute: " + fontSize_ );
            }
        }

        String fontFamily_ = attr.getValue( "font-family" );
        if (fontFamily_ != null) {
            try {
                setFontFamily( fontFamily_ );
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'font-family' attribute: " + fontFamily_ );
            }
        }

        String fontStyle_ = attr.getValue( "font-style" );
        if (fontStyle_ != null) {
            try {
                setFontStyle( SVGFontStyle.parseFontStyle( fontStyle_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'font-style' attribute: " + fontStyle_ );
            }
        }

        String fontWeight_ = attr.getValue( "font-weight" );
        if (fontWeight_ != null) {
            try {
                setFontWeight( SVGFontWeight.parseFontWeight( fontWeight_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'font-weight' attribute: " + fontWeight );
            }
        }

    }

    void setParent( SVGContainer parent ) {
        this.parent = parent;
    }

    public SVGContainer getParent() {
        return parent;
    }

    public void setTitle( String title ) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }

    public void setDescription( String description ) {
        this.desc = description;
    }

    public String getDescription() {
        return desc;
    }

    public void setFillColor( Color color ) {
        fillColor = color;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setStrokeColor( Color color ) {
        strokeColor = color;
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeWidth( Number width ) {
        strokeWidth = width;
    }

    public Number getStrokeWidth() {
        return strokeWidth;
    }

    public void setTransform( AffineTransform transform ) {
        this.transform = transform;
    }

    public AffineTransform getTransform() {
        return transform;
    }

    public void setFontSize( Number fontSize ) {
        this.fontSize = fontSize;
    }

    public Number getFoneSize() {
        return fontSize;
    }

    public void setFontFamily( String fontFamily ) {
        this.fontFamily = fontFamily;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontStyle( SVGFontStyle fontStyle ) {
        this.fontStyle = fontStyle;
    }

    public SVGFontStyle getFontStyle() {
        return fontStyle;
    }

    public void setFontWeight( SVGFontWeight fontWeight ) {
        this.fontWeight = fontWeight;
    }

    public SVGFontWeight getFontWeight() {
        return fontWeight;
    }

    public Element getXML( Document doc ) {
        Element res = doc.createElementNS( XMLNS_SVG, tag );
        if (fillColor != null) {
            res.setAttribute( "fill", SVGColor.toString( fillColor ));
        }
        if (strokeColor != null) {
            res.setAttribute( "stroke", SVGColor.toString( strokeColor ));
        }
        if (strokeWidth != null) {
            res.setAttribute( "stroke-width", SVGNumber.toString( strokeWidth ));
        }
        if (transform != null && !transform.isIdentity()) {
            res.setAttribute( "transform", SVGTransform.toString( transform ));
        }
        if (fontSize != null) {
            res.setAttribute( "font-size", SVGNumber.toString( fontSize ));
        }
        if (fontFamily != null) {
            res.setAttribute( "font-family", fontFamily );
        }
        if (fontStyle != null) {
            res.setAttribute( "font-style", SVGFontStyle.toString( fontStyle ));
        }
        if (fontWeight != null) {
            res.setAttribute( "font-weight", SVGFontWeight.toString( fontWeight ));
        }
        if (title != null) {
            Element e = doc.createElement( "title" );
            e.appendChild( doc.createTextNode( title ));
            res.appendChild( e );
        }
        if (desc != null) {
            Element e = doc.createElement( "desc" );
            e.appendChild( doc.createTextNode( desc ));
            res.appendChild( e );
        }
        return res;
    }

    public final void paint( Graphics g ) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setStroke( DEFAULT_STROKE );
        g2.setFont( DEFAULT_FONT );
        paint( g2, DEFAULT_FILL_COLOR, DEFAULT_STROKE_COLOR );
    }

    protected final void paint( Graphics2D g, Color fillColor, Color strokeColor ) {

        AffineTransform xform0 = g.getTransform();
        if (xform0 == null) {
            xform0 = new AffineTransform();
        }
        if (transform != null) {
            g.transform( transform );
        }

        Stroke stroke0 = g.getStroke();
        if (strokeWidth != null) {
            g.setStroke( new BasicStroke( strokeWidth.floatValue()));
        }

        Font font0 = g.getFont();
        if (hasFontAttributes()) {
            g.setFont( getFont());
        }

        if (this.fillColor != null) {
            fillColor = this.fillColor;
        }
        if (this.strokeColor != null) {
            strokeColor = this.strokeColor;
        }

        paintContents( g, fillColor, strokeColor );

        g.setFont( font0 );
        g.setStroke( stroke0 );
        g.setTransform( xform0 );

    }

    protected abstract void paintContents( Graphics2D g, Color fillColor, Color strokeColor );

    protected abstract Rectangle2D getContentsBounds();

    public final Rectangle2D getBounds() {
        Rectangle2D res = getContentsBounds();
        if (res != null && transform != null) {
            res = transform.createTransformedShape( res ).getBounds2D();
        }
        return res;
    }

    protected abstract void setContentsBounds( Rectangle2D r ) throws NoninvertibleTransformException;

    public final void setBounds( Rectangle2D r ) throws NoninvertibleTransformException {
        if (r == null) {
            throw new NullPointerException();
        }
        if (transform != null) {
            AffineTransform inverse = transform.createInverse();
            r = inverse.createTransformedShape( r ).getBounds2D();
        }
        setContentsBounds( r );
    }
    
    protected abstract Shape getContentsOutline();

    public final Shape getOutline() {
        Shape res = getContentsOutline();
        if (res == null || transform == null) {
            return res;
        }
        return transform.createTransformedShape( res );
    }

    private boolean hasFontAttributes() {
        return fontSize != null || fontFamily != null || fontStyle != null || fontWeight != null;
    }

    /*
    protected abstract void transformContentsBounds( AffineTransform xform );

    protected void transformBounds( AffineTransform xform ) {
        if (transform == null) {
            transform = new AffineTransform(); // Identity transform
        }

        double det = transform.getDeterminant();
        if (det <= 0) {
            return;
        }

        // Creating a transformation that eliminates rotation of the
        // current component
        double q = Math.sqrt( det );
        AffineTransform unrotate = new AffineTransform(
                transform.getScaleY() / q,  // Sx
               -transform.getShearY() / q,  // Ry
               -transform.getShearX() / q,  // Rx
                transform.getScaleX() / q,  // Sy
                0, 0                        // Tx, Ty
        );

        // Converting the current component's transformation to a new
        // one, which includes only translation and scaling.
        AffineTransform translateAndScaleXform =  new AffineTransform( transform );
        translateAndScaleXform.concatenate( unrotate );

        // Removing that translation and scaling from the current transformation
        try {
            AffineTransform unscale = translateAndScaleXform.createInverse();
            transform.concatenate( unscale );
        } catch (NoninvertibleTransformException ex) {
            throw new RuntimeException( ex );
        }

        xform = new AffineTransform( xform );
        xform.concatenate( translateAndScaleXform );
        transformContentsBounds( xform );
        
    }

    public final void transformBounds() {
        transformBounds( new AffineTransform());
    }
    */

    public Font getFont() {

        Font font0 = (parent != null) ? parent.getFont() : DEFAULT_FONT;

        if (!hasFontAttributes()) {
            return font0;
        }

        String family = (fontFamily == null) ? font0.getFamily() : fontFamily;
        if ("serif".equalsIgnoreCase( family )) {
            family = Font.SERIF;
        } else if ("sans-serif".equalsIgnoreCase( family )) {
            family = Font.SANS_SERIF;
        } else if ("monospace".equalsIgnoreCase( family )) {
            family = Font.MONOSPACED;
        }

        int style = 0;
        if ((fontStyle == null && font0.isItalic())
                || fontStyle == SVGFontStyle.italic) {
            style |= Font.ITALIC;
        }
        if ((fontWeight == null && font0.isBold())
                || fontWeight == SVGFontWeight.bold) {
            style |= Font.BOLD;
        }

        int size = fontSize == null ? font0.getSize() : fontSize.intValue();

        return new Font( family, style, size );
        
    }

}
