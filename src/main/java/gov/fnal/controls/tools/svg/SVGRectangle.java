//  (c) 2009 Fermi Research Alliance
//  $Id: SVGRectangle.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:00 $
 */

@SVGTag( "rect" )
public class SVGRectangle extends SVGShape {

    private static final Logger log = Logger.getLogger( SVGRectangle.class.getName());

    private Number x, y, width, height, rx, ry;
    private RoundRectangle2D shape;
    private boolean changed;

    public SVGRectangle() {}

    @Override
    public void applyAttributes( Attributes attr ) {

        super.applyAttributes( attr );

        String x_ = attr.getValue( "x" );
        if (x_ != null) {
            try {
                setX( SVGNumber.parseNumber( x_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'x' attribute: " + x_ );
            }
        }

        String y_ = attr.getValue( "y" );
        if (y_ != null) {
            try {
                setY( SVGNumber.parseNumber( y_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'y' attribute: " + y_ );
            }
        }

        String width_ = attr.getValue( "width" );
        if (width_ != null) {
            try {
                setWidth( SVGNumber.parseNumber( width_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'width' attribute: " + width_ );
            }
        }

        String height_ = attr.getValue( "height" );
        if (height_ != null) {
            try {
                setHeight( SVGNumber.parseNumber( height_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'height' attribute: " + height_ );
            }
        }

        String rx_ = attr.getValue( "rx" );
        if (rx_ != null) {
            try {
                setArcWidth( SVGNumber.parseNumber( rx_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'rx' attribute: " + rx_ );
            }
        }

        String ry_ = attr.getValue( "ry" );
        if (ry_ != null) {
            try {
                setArcHeight( SVGNumber.parseNumber( ry_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'ry' attribute: " + ry_ );
            }
        }

    }

    public void setX( Number x ) {
        this.x = x;
        changed = true;
    }

    public Number getX() {
        return x;
    }

    private double x() {
        return (x == null) ? 0 : x.doubleValue();
    }

    public void setY( Number y ) {
        this.y = y;
        changed = true;
    }

    public Number getY() {
        return y;
    }

    private double y() {
        return (y == null) ? 0 : y.doubleValue();
    }

    public void setWidth( Number width ) {
        this.width = width;
        changed = true;
    }

    public Number getWidth() {
        return width;
    }

    private double width() {
        if (width != null && width.doubleValue() > 0) {
            return width.doubleValue();
        } else {
            return 0;
        }
    }

    public void setHeight( Number height ) {
        this.height = height;
        changed = true;
    }

    public Number getHeight() {
        return height;
    }

    private double height() {
        if (height != null && height.doubleValue() > 0) {
            return height.doubleValue();
        } else {
            return 0;
        }
    }

    public void setArcWidth( Number rx ) {
        this.rx = rx;
        changed = true;
    }

    public Number getArcWidth() {
        return rx;
    }

    private double rx() {
        if (rx != null && rx.doubleValue() > 0) {
            return rx.doubleValue();
        } else {
            return 0;
        }
    }

    public void setArcHeight( Number ry ) {
        this.ry = ry;
        changed = true;
    }

    public Number getArcHeight() {
        return ry;
    }

    private double ry() {
        if (ry != null && ry.doubleValue() > 0) {
            return ry.doubleValue();
        } else {
            return 0;
        }
    }

    @Override
    protected void setContentsBounds( Rectangle2D bounds ) {

        double sx = (width() == 0) ? 0 : bounds.getWidth() / width();
        double sy = (height() == 0) ? 0 : bounds.getHeight() / height();

        setX( bounds.getX());
        setY( bounds.getY());

        setWidth( bounds.getWidth());
        setHeight( bounds.getHeight());

        setArcWidth( rx() * sx );
        setArcHeight( ry() * sy );

    }

    @Override
    protected RoundRectangle2D getShape() {
        if (shape == null || changed) {
            shape = new RoundRectangle2D.Double(
                x(),
                y(),
                width(),
                height(),
                rx(),
                ry()
            );
            changed = false;
        }
        return shape;
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
        if (width != null) {
            res.setAttribute( "width", SVGNumber.toString( width ));
        }
        if (height != null) {
            res.setAttribute( "height", SVGNumber.toString( height ));
        }
        if (rx != null) {
            res.setAttribute( "rx", SVGNumber.toString( rx ));
        }
        if (ry != null) {
            res.setAttribute( "ry", SVGNumber.toString( ry ));
        }
        return res;
    }

}
