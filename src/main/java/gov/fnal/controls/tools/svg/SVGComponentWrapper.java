//  (c) 2009 Fermi Research Alliance
//  $Id: SVGComponentWrapper.java,v 1.1 2009/07/27 21:03:00 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2009/07/27 21:03:00 $
 */

public class SVGComponentWrapper extends Component {
    
    private final SVGComponent comp;

    public SVGComponentWrapper( SVGComponent comp ) {
        if (comp == null) {
            throw new NullPointerException();
        }
        this.comp = comp;
        setLocation( 0, 0 );
        setSize( -1, -1 );
    }

    public SVGComponent getSVGComponent() {
        return comp;
    }

    @Override
    public int getWidth() {
        int res = super.getWidth();
        if (res < 0 && getParent() != null) {
            return getParent().getWidth() - getX();
        } else {
            return res;
        }
    }

    @Override
    public int getHeight() {
        int res = super.getHeight();
        if (res < 0 && getParent() != null) {
            return getParent().getHeight() - getY();
        } else {
            return res;
        }
    }

    @Override
    public Dimension getSize() {
        return new Dimension( getWidth(), getHeight());
    }


    @Override
    public Dimension getMinimumSize() {
        return new Dimension();
    }

    @Override
    public Dimension getMaximumSize() {
        Container cont = getParent();
        return (cont == null) ? new Dimension() : cont.getSize();
    }

    @Override
    public Dimension getPreferredSize() {
        return getMaximumSize();
    }


    /*
    @Override
    protected void validateTree() {
        if (!isValid()) {
            Rectangle2D r = comp.getBounds();
            if (r != null) {
                bounds = r.getBounds();
                // The getBounds does not take into the account the stroke width;
                // for vertical and horizontal line the returned width and hight will be zero.
                // This situation shall be corrected, because a Swing object with
                // zero width or height is not rendered.
                if (bounds.width == 0) {
                    bounds.width = 1;
                }
                if (bounds.height == 0) {
                    bounds.height = 1;
                }
                setSize( bounds.x + bounds.width, bounds.y + bounds.height );
            } else {
                bounds = null;
            }
        }
        super.validateTree();
    }
    */

    @Override
    public void paint( Graphics g ) {

        Graphics2D g2 = (Graphics2D)g;
        
        AffineTransform xform = g2.getTransform();

        if (comp instanceof SVGSvg) {
            Number x = ((SVGSvg)comp).getX();
            Number y = ((SVGSvg)comp).getY();
            g2.translate(
                (x == null) ? 0 : x.doubleValue(),
                (y == null) ? 0 : y.doubleValue()
            );
        }

        comp.paint( g );

        g2.setTransform( xform );

    }
    
}
