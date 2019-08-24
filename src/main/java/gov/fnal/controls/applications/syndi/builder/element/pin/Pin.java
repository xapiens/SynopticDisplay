// (c) 2001-2010 Fermi Research Allaince
// $Id: Pin.java,v 1.2 2010/09/15 16:11:48 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element.pin;

import gov.fnal.controls.applications.syndi.builder.element.AbstractComponent;
import gov.fnal.controls.applications.syndi.builder.element.GenericContainer;
import gov.fnal.controls.applications.syndi.builder.element.ComponentLink;
import java.awt.Point;
import java.awt.geom.Point2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:11:48 $
 */
public class Pin implements Comparable<Pin> {
    
    private ComponentLink sourceLink = null;
    private ComponentLink targetLink = null;

    private float x, y;
    
    private final AbstractComponent comp;
    private final int index;
    private final PinType type;
    private final String name;
    private final PinAddress innerAddress;

    protected Pin( AbstractComponent comp, int index, PinType type, String name ) {
        assert comp != null;
        assert type != null;
        this.comp = comp;
        this.index = index;
        this.type = type;
        this.innerAddress = new PinAddress( 0, index );
        if (name != null) {
            name = name.trim();
            if ("".equals( name )) {
                name = null;
            }
        }
        this.name = name;
    }

    public AbstractComponent getComponent() {
        return comp;
    }

    public int getIndex() {
        return index;
    }

    public PinType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    
    public void setLocation( Point2D p ) {
        if (x == p.getX() && y == p.getY()) {
            return;
        }
        x = (float)p.getX();
        y = (float)p.getY();
        adjust();
    }
    
    public Point2D getLocation() {
        return new Point2D.Float( x, y );
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Point getLocationOnComponent() {
        return new Point(
            Math.round( x * (comp.getWidth() - 1)),
            Math.round( y * (comp.getHeight() - 1))
        );
    }

    public PinRole roleFor( GenericContainer scope ) {
        if (type == PinType.INPUT) {
            return (scope == comp) ? PinRole.SOURCE : PinRole.TARGET;
        } else {
            return (scope == comp) ? PinRole.TARGET : PinRole.SOURCE;
        }
    }
    
    public PinAddress getAddress( GenericContainer scope ) {
        if (scope == comp) {
            return innerAddress;
        } else {
            return getOuterAddress();
        }
    }
    
    public PinAddress getAddress( PinRole role ) {
        if ((type == PinType.INPUT && role == PinRole.TARGET)
                || (type == PinType.OUTPUT && role == PinRole.SOURCE)) {
            return getOuterAddress();
        } else {
            return innerAddress;
        }
    }

    private PinAddress getOuterAddress() {
        Integer compId = comp.getId();
        return new PinAddress( (compId == null) ? 0 : compId.intValue(), index );
    }
    
    public void setLink( GenericContainer scope, ComponentLink link ) {
        setLink( roleFor( scope ), link );
    }

    public void setLink( PinRole role, ComponentLink link ) {
        if (role == null) {
            throw new NullPointerException();
        }
        if (role == PinRole.SOURCE) {
            sourceLink = link;
        } else {
            targetLink = link;
        }
    }
    
    public ComponentLink getLink( GenericContainer scope ) {
        return getLink( roleFor( scope ));
    }

    public ComponentLink getLink( PinRole role ) {
        if (role == null) {
            throw new NullPointerException();
        }
        return (role == PinRole.SOURCE) ? sourceLink : targetLink;
    }
    
    public void adjust() {
        if (sourceLink != null) {
            sourceLink.adjust();
        }
        if (targetLink != null) {
            targetLink.adjust();
        }
    }
    
    public boolean isInUse() {
        return sourceLink != null || targetLink != null;
    }
    
    public Element getXML( Document doc ) {
        Element res = doc.createElement( type == PinType.INPUT ? "input" : "output" );
        res.setAttribute( "number", String.valueOf( index ));
        res.setAttribute( "x", String.valueOf( x ));
        res.setAttribute( "y", String.valueOf( y ));
        if (name != null) {
            res.setAttribute( "name", name );
        }
        return res;
    }

    @Override
    public String toString() {
        return String.format( "%s[address=%s;type=%s;name=%s]", 
                getClass().getSimpleName(), getOuterAddress(), type, name );
    }

    @Override
    public int compareTo( Pin pin ) {
        int res = this.type.ordinal() - pin.type.ordinal();
        if (res != 0) {
            return res;
        }
        return this.index - pin.index;
    }
    
}
