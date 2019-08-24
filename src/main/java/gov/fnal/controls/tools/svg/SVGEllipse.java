//  (c) 2009 Fermi Research Alliance
//  $Id: SVGEllipse.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:00 $
 */

@SVGTag( "ellipse" )
public class SVGEllipse extends SVGShape {

    private static final Logger log = Logger.getLogger( SVGEllipse.class.getName());

    private Number cx, cy, rx, ry;
    private Ellipse2D shape;
    private boolean changed;

    public SVGEllipse() {}

    @Override
    public void applyAttributes( Attributes attr ) {

        super.applyAttributes( attr );

        String cx_ = attr.getValue( "cx" );
        if (cx_ != null) {
            try {
                setCenterX( SVGNumber.parseNumber( cx_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'cx' attribute: " + cx_ );
            }
        }

        String cy_ = attr.getValue( "cy" );
        if (cy_ != null) {
            try {
                setCenterY( SVGNumber.parseNumber( cy_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'cy' attribute: " + cy_ );
            }
        }

        String rx_ = attr.getValue( "rx" );
        if (rx_ != null) {
            try {
                setRadiusX( SVGNumber.parseNumber( rx_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'rx' attribute: " + rx_ );
            }
        }

        String ry_ = attr.getValue( "ry" );
        if (ry_ != null) {
            try {
                setRadiusY( SVGNumber.parseNumber( ry_ ));
            } catch (IllegalArgumentException ex) {
                log.warning( "Illegal 'ry' attribute: " + ry_ );
            }
        }

    }

    public void setCenterX( Number cx ) {
        this.cx = cx;
        changed = true;
    }

    public Number getCenterX() {
        return cx;
    }

    private double cx() {
        return (cx == null) ? 0 : cx.doubleValue();
    }

    public void setCenterY( Number cy ) {
        this.cy = cy;
        changed = true;
    }

    public Number getCenterY() {
        return cy;
    }

    private double cy() {
        return (cy == null) ? 0 : cy.doubleValue();
    }

    public void setRadiusX( Number rx ) {
        this.rx = rx;
        changed = true;
    }

    public Number getRadiusX() {
        return rx;
    }

    private double rx() {
        if (rx != null && rx.doubleValue() > 0) {
            return rx.doubleValue();
        } else {
            return 0;
        }
    }

    public void setRadiusY( Number ry ) {
        this.ry = ry;
        changed = true;
    }

    public Number getRadiusY() {
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
    protected Ellipse2D getShape() {
        if (shape == null || changed) {
            double rx_ = rx();
            double ry_ = ry();
            shape = new Ellipse2D.Double(
                    cx() - rx_,
                    cy() - ry_,
                    2f * rx_,
                    2f * ry_
            );
            changed = false;
        }
        return shape;
    }

    @Override
    protected void setContentsBounds( Rectangle2D bounds ) {
        setCenterX( bounds.getCenterX());
        setCenterY( bounds.getCenterY());
        setRadiusX( bounds.getWidth() / 2.0 );
        setRadiusY( bounds.getHeight() / 2.0 );
    }

    @Override
    public Element getXML( Document doc ) {
        Element res = super.getXML( doc );
        if (cx != null) {
            res.setAttribute( "cx", SVGNumber.toString( cx ));
        }
        if (cy != null) {
            res.setAttribute( "cy", SVGNumber.toString( cy ));
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
