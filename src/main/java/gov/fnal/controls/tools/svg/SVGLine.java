//  (c) 2009 Fermi Research Alliance
//  $Id: SVGLine.java,v 1.1 2009/07/27 21:03:01 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:01 $
 */

@SVGTag( "line" )
public class SVGLine extends SVGShape {

    private static final Logger log = Logger.getLogger( SVGLine.class.getName());

    private Number x1, x2, y1, y2;
    private Line2D shape;
    private boolean changed;

    public SVGLine() {}

    @Override
    public void applyAttributes( Attributes attr ) {

        super.applyAttributes( attr );

        String x1_ = attr.getValue( "x1" );
        if (x1_ != null) {
            try {
                setX1( SVGNumber.parseNumber( x1_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'x1' attribute: " + x1_ );
            }
        }

        String x2_ = attr.getValue( "x2" );
        if (x2_ != null) {
            try {
                setX2( SVGNumber.parseNumber( x2_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'x2' attribute: " + x2_ );
            }
        }

        String y1_ = attr.getValue( "y1" );
        if (y1_ != null) {
            try {
                setY1( SVGNumber.parseNumber( y1_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'y1' attribute: " + y1_ );
            }
        }

        String y2_ = attr.getValue( "y2" );
        if (y2_ != null) {
            try {
                setY2( SVGNumber.parseNumber( y2_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'y2' attribute: " + y2_ );
            }
        }

    }

    public void setX1( Number x1 ) {
        this.x1 = x1;
        changed = true;
    }

    public Number getX1() {
        return x1;
    }

    private double x1() {
        return (x1 == null) ? 0 : x1.doubleValue();
    }

    public void setX2( Number x2 ) {
        this.x2 = x2;
        changed = true;
    }

    public Number getX2() {
        return x2;
    }

    private double x2() {
        return (x2 == null) ? 0 : x2.doubleValue();
    }

    public void setY1( Number y1 ) {
        this.y1 = y1;
        changed = true;
    }

    public Number getY1() {
        return y1;
    }

    private double y1() {
        return (y1 == null) ? 0 : y1.doubleValue();
    }

    public void setY2( Number y2 ) {
        this.y2 = y2;
        changed = true;
    }

    public Number getY2() {
        return y2;
    }

    private double y2() {
        return (y2 == null) ? 0 : y2.doubleValue();
    }

    @Override
    protected Line2D getShape() {
        if (shape == null || changed) {
            shape = new Line2D.Double( x1(), y1(), x2(), y2());
        }
        return shape;
    }

    @Override
    protected void setContentsBounds( Rectangle2D bounds ) {
        Rectangle2D r = getContentsBounds();
        double sx = (r.getWidth() == 0) ? 0 : bounds.getWidth() / r.getWidth();
        double sy = (r.getHeight() == 0) ? 0 : bounds.getHeight() / r.getHeight();
        setX1( bounds.getX() + (x1() - r.getX()) * sx );
        setX2( bounds.getX() + (x2() - r.getX()) * sx );
        setY1( bounds.getY() + (y1() - r.getY()) * sy );
        setY2( bounds.getY() + (y2() - r.getY()) * sy );
    }

    @Override
    public Element getXML( Document doc ) {
        Element res = super.getXML( doc );
        if (x1 != null) {
            res.setAttribute( "x1", SVGNumber.toString( x1 ));
        }
        if (y1 != null) {
            res.setAttribute( "y1", SVGNumber.toString( y1 ));
        }
        if (x2 != null) {
            res.setAttribute( "x2", SVGNumber.toString( x2 ));
        }
        if (y2 != null) {
            res.setAttribute( "y2", SVGNumber.toString( y2 ));
        }
        return res;
    }

}
