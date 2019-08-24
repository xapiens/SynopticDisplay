//  (c) 2010 Fermi Research Alliance
//  $Id: SVGShape.java,v 1.3 2010/02/12 21:03:20 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

public abstract class SVGShape extends SVGComponent {

    protected SVGShape() {}

    @Override
    protected void paintContents( Graphics2D g, Color fillColor, Color strokeColor ) {
        Shape shape = getShape();
        if (shape == null) {
            return;
        }
        if (fillColor != null && fillColor != SVGColor.NO_COLOR) {
            g.setColor( fillColor );
            g.fill( shape );
        }
        if (strokeColor != null && strokeColor != SVGColor.NO_COLOR) {
            g.setColor( strokeColor );
            g.draw( shape );
        }
    }

    @Override
    public void setText( String text ) {}

    @Override
    protected Rectangle2D getContentsBounds() {
        Shape shape = getShape();
        return (shape != null) ? shape.getBounds2D() : null;
    }

    @Override
    protected abstract void setContentsBounds( Rectangle2D r );

    @Override
    protected Shape getContentsOutline() {
        return getShape();
    }

    /*
    @Override
    protected void transformContentsBounds( AffineTransform xform ) {
        Rectangle2D r0 = getContentsBounds();
        Rectangle2D r1 = xform.createTransformedShape( r0 ).getBounds2D();
        setContentsBounds( r1 );
    }
     */

    protected abstract Shape getShape();

}
