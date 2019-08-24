// (c) 2001-2010 Fermi Research Allaince
// $Id: AbstractComponent.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.SynopticConfig;
import gov.fnal.controls.applications.syndi.util.ImageFactory;
import gov.fnal.controls.applications.syndi.builder.element.pin.Pin;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinCollection;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinRole;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.IntegerProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public abstract class AbstractComponent implements BuilderComponent, Config {
    
    protected final PropertyCollection props = new PropertyCollection();
    protected final PinCollection pins = new PinCollection( this );

    private int x = 0;
    private int y = 0;
    private int width = MIN_SIZE;
    private int height = MIN_SIZE;
    private String name = null;
    private Integer id = null;
    private BuilderContainer parent = null;
    private String description, helpUrl, runtimeClass;
    private Color bgColor = null;
    private Image bgImage = null;
    private boolean selected = false;
    
    protected AbstractComponent() {
        // A standard set of properties
        props.add( new IntegerProperty( "x", "X", true ));
        props.add( new IntegerProperty( "y", "Y", true ));
        props.add( new IntegerProperty( "width", "Width", true ));
        props.add( new IntegerProperty( "height", "Height", true ));
    }
    
    @Override
    public void setParent( BuilderContainer parent ) {
        BuilderContainer p0 = parent;
        this.parent = parent;
        firePropertyChange( "parent", p0, parent );
    }

    @Override
    public BuilderContainer getParent() {
        return parent;
    }

    @Override
    public void setName( String name ) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCaption() {
        return getName();
    }
    
    @Override
    public ComponentType getType() {
        return null;
    }
    
    @Override
    public void setDescription( String desc ) {
        this.description = desc;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public void setHelpUrl( String helpUrl ) {
        this.helpUrl = helpUrl;
    }
    
    @Override
    public String getHelpUrl() {
        return helpUrl;
    }

    public boolean isBackgroundImageEnabled() {
        return getType() == ComponentType.VISUAL;
    }
    
    public void setBackgroundImage( Image val ) {
        bgImage = val;
    }

    public Image getBackgroundImage() {
        return bgImage;
    }
    
    public Color getBackgroundColor() {
        return bgColor;
    }
    
    @Override
    public void setSelected( boolean val ) {
        selected = val;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }
    
    @Override
    public void setLocation( int x, int y ) {
        if (parent instanceof GenericContainer) {
            if (x < 0) {
                x = 0;
            } else {
                x = Math.min( x, ((GenericContainer)parent).getWidth() - width );
            }
            if (y < 0) {
                y = 0;
            } else {
                y = Math.min( y, ((GenericContainer)parent).getHeight() - height );
            }
        }
        if (this.x == x && this.y == y) {
            return;
        }
        Point p0 = new Point( this.x, this.y );
        Point p1 = new Point( x, y );
        this.x = x;
        this.y = y;
        firePropertyChange( "location", p0, p1 );
    }
    
    @Override
    public int getX() {
        return x;
    }
    
    @Override
    public int getY() {
        return y;
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle( x, y, width, height );
    }
    
    @Override
    public boolean contains( Point p ) {
        return p.x >= x - HIT_TOLERANCE &&
               p.x <  x + width + HIT_TOLERANCE &&
               p.y >= y - HIT_TOLERANCE &&
               p.y <  y + height + HIT_TOLERANCE;
    }
    
    @Override
    public abstract Shape getMovingShape( Point dp );
    
    @Override
    public abstract void paint( Graphics2D g );

    public void setId( Integer id ) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public PinCollection pins() {
        return pins;
    }
    
    public Collection<ComponentLink> links() {
        Collection<ComponentLink> res = new HashSet<ComponentLink>();
        ComponentLink l;
        for (Pin pin : pins) {
            l = pin.getLink( PinRole.SOURCE );
            if (l != null) {
                res.add( l );
            }
            l = pin.getLink( PinRole.TARGET );
            if (l != null) {
                res.add( l );
            }
        }
        return res;
    }
    
    public abstract Pin getPinAt( int x, int y );
    
    public void setSize( int width, int height ) {
        if (parent instanceof GenericContainer) {
            if (width < MIN_SIZE) {
                width = MIN_SIZE;
            } else {
                width = Math.min( width, ((GenericContainer)parent).getWidth() - x );
            }
            if (height < MIN_SIZE) {
                height = MIN_SIZE;
            } else {
                height = Math.min( height, ((GenericContainer)parent).getHeight() - y );
            }
        }
        if (this.width == width && this.height == height) {
            return;
        }
        Dimension d0 = new Dimension( this.width, this.height );
        Dimension d1 = new Dimension( width, height );
        this.width = width;
        this.height = height;
        firePropertyChange( "size", d0, d1 );
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }

    public void setRuntimeClass( String className ) {
        this.runtimeClass = className;
    }

    public String getRuntimeClass() {
        return runtimeClass;
    }
    
    @Override
    public void setProperties( Collection<ComponentProperty<?>> val ) throws PropertyException {

        props.addAll( val );

        props.removeByName( "minWidth" );
        props.removeByName( "minHeight" );

        setLocation(
            props.getValue( Integer.class, "x", 0 ),
            props.getValue( Integer.class, "y", 0 )
        );
        setSize(
            props.getValue( Integer.class, "width", MIN_SIZE ),
            props.getValue( Integer.class, "height", MIN_SIZE )
        );

        bgColor = props.getValue( Color.class, "background" ); // can be null

        if (props.hasValue( "name" )) {
            setName( props.getValue( String.class, "name" ));
        }
        props.removeByName( "name" );

        if (props.hasValue( "minInputs" )) {
            pins.setMinInputCount( props.getValue( Integer.class, "minInputs" ));
        }
        props.removeByName( "minInputs" );

        if (props.hasValue( "maxInputs" )) {
            pins.setMaxInputCount( props.getValue( Integer.class, "maxInputs" ));
        }
        props.removeByName( "maxInputs" );

        if (props.hasValue( "minOutputs" )) {
            pins.setMinOutputCount( props.getValue( Integer.class, "minOutputs" ));
        }
        props.removeByName( "minOutputs" );

        if (props.hasValue( "maxOutputs" )) {
            pins.setMaxOutputCount( props.getValue( Integer.class, "maxOutputs" ));
        }
        props.removeByName( "maxOutputs" );

    }
    
    @Override
    public Collection<ComponentProperty<?>> getProperties() {
        try {
            props.get( IntegerProperty.class, "x" ).setValue( x );
            props.get( IntegerProperty.class, "y" ).setValue( y );
            props.get( IntegerProperty.class, "width" ).setValue( width );
            props.get( IntegerProperty.class, "height" ).setValue( height );
        } catch (PropertyException ex) {
            throw new RuntimeException( ex ); // should not happen
        }
        PropertyCollection res = (PropertyCollection)props.clone();
        res.setComponent( this );
        return res;
    }
    
    @Override
    public Element getXML( Document doc ) {
        Element res = doc.createElementNS( SynopticConfig.DISPLAY_NS, "element" );
        if (getId() != null) {
            res.setAttribute( "id", getId().toString());
        }
        if (name != null) {
            res.setAttribute( "name", name );
        }
        String builderClass = getClass().getName();
        if (!builderClass.equals( BuilderComponentFactory.DEFAULT_COMPONENT )) {
            res.setAttribute( "designTimeView", builderClass );
        }
        if (runtimeClass != null) {
            res.setAttribute( "implementation", runtimeClass );
        }
        if (helpUrl != null) {
            res.setAttribute( "help-url", helpUrl );
        }
        if (description != null) {
            Element e = doc.createElement( "desc" );
            e.appendChild( doc.createTextNode( description ));
            res.appendChild( e );
        }
        if (bgImage != null) {
            Element e = doc.createElement( "bkimage" );
            e.setAttribute( "type", ImageFactory.DEFAULT_IMAGE_TYPE );
            String image64 = ImageFactory.encode( bgImage );
            e.appendChild( doc.createTextNode( image64 ));
            res.appendChild( e );
        }
        for (ComponentProperty prop : getProperties()) {
            res.appendChild( prop.getXML( doc ));
        }
        res.setAttribute( "minInputs", String.valueOf( pins.getMinInputCount()));
        res.setAttribute( "maxInputs", String.valueOf( pins.getMaxInputCount()));
        res.setAttribute( "minOutputs", String.valueOf( pins.getMinOutputCount()));
        res.setAttribute( "maxOutputs", String.valueOf( pins.getMaxOutputCount()));
        for (Pin pin : pins()) {
            res.appendChild( pin.getXML( doc ));
        }
        return res;
    }
    
    @Override
    public String toString() {
        Integer id_ = getId();
        if (id_ == null) {
            return getName();
        } else {
            return getName() + " #" + id_;
        }
    }
    
    protected void firePropertyChange( String name, Object oldValue, Object newValue ) {
        PropertyChangeEvent e = new PropertyChangeEvent( this, name, oldValue, newValue );
        if ("location".equals( name ) || "size".equals( name ) || "parent".equals( name )) {
            for (ComponentLink link : links()) {
                link.adjust();
            }
        }
        if (parent instanceof PropertyChangeListener) {
            ((PropertyChangeListener)parent).propertyChange( e );
        }
    }

}
