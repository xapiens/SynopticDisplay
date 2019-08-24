// (c) 2001-2010 Fermi Research Allaince
// $Id: ComponentLink.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.element.pin.Pin;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinAddress;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinRole;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.svg.SVGColor;
import gov.fnal.controls.tools.svg.SVGPath;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Collection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class ComponentLink extends GenericLine {

    private Pin sourcePin, targetPin;
    private PinAddress source, target;
    
    public ComponentLink() {
        this( null, null );
    }

    public ComponentLink( PinAddress source, PinAddress target ) {
        super( new SVGPath());
        svg.setFillColor( SVGColor.NO_COLOR );
        setLinkAttributes();
        this.source = source;
        this.target = target;
    }

    private void setLinkAttributes() {
        setStrokeColor( isSelected() ? LINK_HIGHLIGHT_COLOR : LINK_COLOR ); 
        setStrokeWidth( isSelected() ? LINK_HIGHLIGHT_SIZE : LINK_SIZE );
    }

    @Override
    public void setSelected( boolean val ) {
        super.setSelected( val );
        setLinkAttributes();
    }

    @Override
    public void setParent( BuilderContainer parent ) 
            throws IllegalStateException, IllegalArgumentException {
        if (parent != null) {
            if (!(parent instanceof GenericContainer)) {
                throw new IllegalArgumentException( "Invalid link parent" );
            }
            if (source == null || target == null) {
                throw new IllegalStateException( "Link not connected" );
            }
            GenericContainer cont = (GenericContainer)parent;
            sourcePin = findPin( cont, source );
            targetPin = findPin( cont, target );
            if (sourcePin.getLink( PinRole.SOURCE ) != null ||
                    targetPin.getLink( PinRole.SOURCE ) != null) {
                sourcePin = null;
                targetPin = null;
                throw new IllegalStateException( "Pin already connected" );
            }
            sourcePin.setLink( PinRole.SOURCE, this );
            targetPin.setLink( PinRole.TARGET, this );
        } else {
            if (sourcePin != null) {
                sourcePin.setLink( PinRole.SOURCE, null );
            }
            sourcePin = null;
            if (targetPin != null) {
                targetPin.setLink( PinRole.TARGET, null );
            }
            targetPin = null;
        }
        super.setParent( parent );
        adjust();
    }

    private Pin findPin( GenericContainer cont, PinAddress addr ) throws IllegalArgumentException {
        AbstractComponent comp = cont.getComponentById( addr.getComponentId());
        if (comp == null) {
            throw new IllegalArgumentException( "Component " + addr.getComponentId() + " not found" );
        }
        Pin res = comp.pins().getPin( addr.getPinId());
        if (res == null) {
            throw new IllegalArgumentException( "Pin " + addr + " not found" );
        }
        return res;
    }
    
    public void setPinAddress( PinRole role, PinAddress val ) throws IllegalStateException {
        if (role == null) {
            throw new NullPointerException();
        }
        if (role == PinRole.SOURCE) {
            this.source = val;
        } else {
            this.target = val;
        }
    }
    
    public PinAddress getPinAddress( PinRole role ) {
        if (role == null) {
            throw new NullPointerException();
        }
        return role == PinRole.SOURCE ? source : target;
    }

    public Pin getPin( PinRole role ) {
        if (role == null) {
            throw new NullPointerException();
        }
        return role == PinRole.SOURCE ? sourcePin : targetPin;
    }
    
    public boolean isConnected() {
        return source != null && target != null;
    }
    
    public void adjust() {
        if (sourcePin == null || targetPin == null) {
            return;
        }

        int x0 = getX();
        int y0 = getY();

        Point sourcePoint = getPinPoint( sourcePin );
        Point targetPoint = getPinPoint( targetPin );
        Point innerPoint = null;

        Path2D path0 = getPath();
        Path2D path1 = new Path2D.Float();

        int x1 = sourcePoint.x;
        int y1 = sourcePoint.y;

        int dx = x0 - x1;
        int dy = y0 - y1;

        path1.moveTo( 0, 0 );

        if (path0 != null) {
            int i = 0;
            float[] coor = new float[ 6 ];
            for (PathIterator z = path0.getPathIterator( null ); !z.isDone(); z.next()) {
                if (i++ == 0) {
                    continue;
                }
                if (innerPoint != null) {
                    path1.lineTo( innerPoint.x + dx, innerPoint.y + dy );
                    innerPoint = null;
                }
                switch (z.currentSegment( coor )) {
                    case PathIterator.SEG_MOVETO:
                    case PathIterator.SEG_LINETO:
                        innerPoint = new Point(
                            Math.round( coor[ 0 ]),
                            Math.round( coor[ 1 ])
                        );
                        break;
                    case PathIterator.SEG_QUADTO:
                        innerPoint = new Point(
                            Math.round( coor[ 2 ]),
                            Math.round( coor[ 3 ])
                        );
                        break;
                    case PathIterator.SEG_CUBICTO:
                        innerPoint = new Point(
                            Math.round( coor[ 4 ]),
                            Math.round( coor[ 5 ])
                        );
                        break;
                }
            }
        }
        path1.lineTo( targetPoint.x - x1, targetPoint.y - y1 );

        setPath( path1 );
        setLocation( x1, y1 );
        
    }
    
    private Point getPinPoint( Pin pin ) {
        if (pin == null) {
            return null;
        }
        AbstractComponent comp = pin.getComponent();
        Point res = pin.getLocationOnComponent();
        res.translate( comp.getX(), comp.getY());
        return res;
    }

    @Override
    public String toString() {
        return "Link " + source + "\u2192" + target;
    }

    @Override
    public Element getXML( Document doc ) {
        if (source == null || target == null) {
            throw new IllegalStateException( "Link not connected" );
        }
        Element res = doc.createElement( "link" );
        res.setAttribute( "source", String.valueOf( source.getComponentId()));
        res.setAttribute( "source_pin", String.valueOf( source.getPinId()));
        res.setAttribute( "target", String.valueOf( target.getComponentId()));
        res.setAttribute( "target_pin", String.valueOf( target.getPinId()));
        String pathString = getPathString();
        if (pathString != null) {
            res.setAttribute( "path", pathString );
        }
        return res;
    }

    public void setPathString( String pathString ) {

        Path2D path = SVGPath.toPath( pathString );

        Point2D origin = SVGPath.getFirstPoint( path );
        int x = (int)Math.round( origin.getX());
        int y = (int)Math.round( origin.getY());
        super.setLocation( x, y );

        AffineTransform shift = AffineTransform.getTranslateInstance( -x, -y );
        path.transform( shift );

        svg.setPath( path );
        recalculateBounds();
        
    }

    public String getPathString() {
        AffineTransform shift = AffineTransform.getTranslateInstance( getX(), getY());
        return SVGPath.toString( svg.getPath(), shift );
    }

    @Override
    public Collection<ComponentProperty<?>> getProperties() {
        return new PropertyCollection();
    }

    @Override
    public void setProperties( Collection<ComponentProperty<?>> props ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentType getType() {
        return ComponentType.LINK;
    }

}
