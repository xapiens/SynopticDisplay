//  (c) 2010 Fermi Research Alliance
//  $Id: SVGText.java,v 1.2 2010/02/12 21:03:20 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;

@SVGTag( "text" )
public class SVGText extends SVGComponent {

    private static final Logger log = Logger.getLogger( SVGText.class.getName());
    
    private Number x, y;
    private String text;

    public SVGText() {}

    @Override
    public void applyAttributes( Attributes attr ) {

        super.applyAttributes( attr );

        String x_ = attr.getValue( "x" );
        if (x_ != null) {
            try {
                setX( SVGNumber.parseNumber( x_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Invalid 'x' attribute: " + x_ );
            }
        }

        String y_ = attr.getValue( "y" );
        if (y_ != null) {
            try {
                setY( SVGNumber.parseNumber( y_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Invalid 'y' attribute: " + y_ );
            }
        }

    }

    @Override
    public void setText( String text ) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setX( Number x ) {
        this.x = x;
    }

    public Number getX_() {
        return x;
    }

    private double x() {
        return (x == null) ? 0 : x.doubleValue();
    }

    public void setY( Number y ) {
        this.y = y;
    }

    public Number getY_() {
        return y;
    }

    private double y() {
        return (y == null) ? 0 : y.doubleValue();
    }

    @Override
    protected Rectangle2D getContentsBounds() {
        if (text != null) {
            FontRenderContext context = new FontRenderContext( null, false, false );
            Rectangle2D r = getFont().getStringBounds( text, context );
            return new Rectangle2D.Double(
                    r.getX() + x(),
                    r.getY() + y(),
                    r.getWidth(),
                    r.getHeight()
            );
        } else {
            return new Rectangle2D.Double( x(), y(), 0, 0 );
        }
    }

    @Override
    protected void setContentsBounds( Rectangle2D bounds ) {

        Rectangle2D r0 = getContentsBounds();

        double tx = bounds.getX() - r0.getX();
        double ty = bounds.getY() - r0.getY();

        setX( x() + tx );
        setY( y() + ty );

        double sx = (r0.getWidth() == 0) ? 0 : bounds.getWidth() / r0.getWidth();
        double sy = (r0.getHeight() == 0) ? 0 : bounds.getHeight() / r0.getHeight();

        
        AffineTransform xform = new AffineTransform();
        xform.translate( bounds.getX(), bounds.getY());
        xform.scale( sx, sy );
        xform.translate( -bounds.getX(), -bounds.getY());
        if (!xform.isIdentity()) {
            AffineTransform xform0 = getTransform();
            if (xform0 != null) {
                xform.preConcatenate( xform0 );
            }
            setTransform( xform );
        }
        
    }

    @Override
    protected Shape getContentsOutline() {
        return getContentsBounds();
    }

    /*
    @Override
    protected void transformBounds( AffineTransform xform ) {
        AffineTransform transform = getTransform();
        if (transform == null) {
            transform = new AffineTransform();
        }
        transform.preConcatenate( xform );
        setTransform( transform );
    }

    @Override
    protected void transformContentsBounds( AffineTransform xform ) {}
     */

    @Override
    protected void paintContents( Graphics2D g, Color fillColor, Color strokeColor ) {
        if (text != null && fillColor != null && fillColor != SVGColor.NO_COLOR) {
            g.setColor( fillColor );
            g.drawString( text, (float)x(), (float)y());
        }
    }

    @Override
    public Element getXML( Document doc ) {
        Element res = super.getXML( doc );
        if (x != null) {
            res.setAttribute( "x", SVGNumber.toString( x ));
        }
        if (y != null) {
            res.setAttribute( "y", SVGNumber.toString( y ));
        }
        if (text != null) {
            res.appendChild( doc.createTextNode( text ));
        }
        return res;
    }
    
}
