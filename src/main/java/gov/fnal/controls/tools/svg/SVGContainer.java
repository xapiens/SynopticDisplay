//  (c) 2010 Fermi Research Alliance
//  $Id: SVGContainer.java,v 1.2 2010/02/12 21:03:20 apetrov Exp $
package gov.fnal.controls.tools.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A base container of SVG components.
 *
 * @author Andrey Petrov
 */

@SVGTag( "g" )
public class SVGContainer extends SVGComponent implements Iterable<SVGComponent> {

    private List<SVGComponent> items = new ArrayList<SVGComponent>();

    protected SVGContainer() {}

    @Override
    public boolean add( SVGElement child ) {
        if (super.add( child )) {
            return true;
        }
        if (child instanceof SVGComponent) {
            SVGComponent comp = (SVGComponent)child;
            SVGContainer parent = comp.getParent();
            if (parent != null) {
                throw new IllegalArgumentException( "Already added" );
            }
            items.add( comp );
            comp.setParent( this );
            return true;
        }
        return false;
    }

    @Override
    public void setText( String text ) {}

    @Override
    public Iterator<SVGComponent> iterator() {
        return items.iterator();
    }

    @Override
    protected Rectangle2D getContentsBounds() {
        Rectangle2D res = null;
        for (SVGComponent c : items) {
            Rectangle2D r = c.getBounds();
            if (res == null) {
                res = r;
            } else if (r != null) {
                res.add( r );
            }
        }
        return res;
    }

    @Override
    protected void setContentsBounds( Rectangle2D bounds )
            throws NoninvertibleTransformException {
        Rectangle2D r0 = getContentsBounds();
        if (r0 == null) {
            return;
        }
        double sx = (r0.getWidth() == 0) ? 0 : bounds.getWidth() / r0.getWidth();
        double sy = (r0.getHeight() == 0) ? 0 : bounds.getHeight() / r0.getHeight();
        for (SVGComponent c : items) {
            Rectangle2D r = c.getBounds();
            if (r == null) {
                continue;
            }
            c.setBounds( 
                new Rectangle2D.Double(
                    bounds.getX() + (r.getX() - r0.getX()) * sx,
                    bounds.getY() + (r.getY() - r0.getY()) * sy,
                    r.getWidth() * sx,
                    r.getHeight() * sy
            ));
        }
    }

    @Override
    protected Shape getContentsOutline() {
        GeneralPath res = new GeneralPath();
        for (SVGComponent c : items) {
            Shape s = c.getOutline();
            if (s != null) {
                res.append( s, false );
            }
        }
        return res;
    }

    /*
    @Override
    protected void transformContentsBounds( AffineTransform xform ) {
        for (SVGComponent c : items) {
            c.transformBounds( xform );
        }
    }
     */

    @Override
    protected void paintContents( Graphics2D g, Color fillColor, Color strokeColor ) {
        for (SVGComponent c : items) {
            c.paint( g, fillColor, strokeColor );
        }
    }

    @Override
    public Element getXML( Document doc ) {
        Element res = super.getXML( doc );
        for (SVGComponent c : items) {
            res.appendChild( c.getXML( doc ));
        }
        return res;
    }
    
}
