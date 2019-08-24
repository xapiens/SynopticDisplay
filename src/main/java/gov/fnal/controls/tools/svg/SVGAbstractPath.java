//  (c) 2009 Fermi Research Alliance
//  $Id: SVGAbstractPath.java,v 1.1 2009/07/27 21:03:01 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:01 $
 */

public abstract class SVGAbstractPath extends SVGShape {

    private Path2D path;

    protected SVGAbstractPath() {}

    public void setPath( Path2D path ) {
        this.path = path;
    }

    public Path2D getPath() {
        return path;
    }

    @Override
    protected void setContentsBounds( Rectangle2D bounds ) {
        if (path == null) {
            return;
        }
        Rectangle2D r0 = path.getBounds2D();
        double sx = (r0.getWidth() == 0) ? 0 : bounds.getWidth() / r0.getWidth();
        double sy = (r0.getHeight() == 0) ? 0 : bounds.getHeight() / r0.getHeight();
        AffineTransform xform = new AffineTransform();
        xform.translate( bounds.getX(), bounds.getY());
        xform.scale( sx, sy );
        xform.translate( -r0.getX(), -r0.getY());
        setPath( new Path2D.Double( path, xform ));
    }

    @Override
    protected Shape getShape() {
        return path;
    }

}
