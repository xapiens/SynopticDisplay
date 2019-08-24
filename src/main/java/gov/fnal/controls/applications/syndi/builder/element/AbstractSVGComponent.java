// (c) 2001-2010 Fermi Research Allaince
// $Id: AbstractSVGComponent.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.SynopticConfig;
import gov.fnal.controls.applications.syndi.builder.CanvasAction;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.tools.svg.SVGComponent;
import gov.fnal.controls.tools.svg.SVGNamespace;
import gov.fnal.controls.tools.svg.SVGNumber;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public abstract class AbstractSVGComponent<T extends SVGComponent> 
        implements BuilderComponent, Config, SVGNamespace {

    private static final Logger log = Logger.getLogger( AbstractSVGComponent.class.getName());
    
    protected static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
    
    protected static final Stroke BASE_STROKE = new BasicStroke();
    
    protected final T svg;

    protected final PropertyCollection props = new PropertyCollection();

    private BuilderContainer parent;
    private String description, name, helpUrl;
    private boolean selected;
    private Rectangle bounds, lastOutlineBounds;
    private Shape outline;
    private int x, y;

    protected GridAttributes cachedGridAttributes = new GridAttributes();
    
    protected AbstractSVGComponent( T svg ) {
        this.svg = svg;
        recalculateBounds();
    }

    @Override
    public Element getXML( Document doc ) {
        Element root = doc.createElementNS( XMLNS_SVG, "svg" );
        root.setAttribute( "x", SVGNumber.toString( x ));
        root.setAttribute( "y", SVGNumber.toString( y ));
        if (name != null) {
            Element e = doc.createElement( "title" );
            e.appendChild( doc.createTextNode( name ));
            root.appendChild( e );
        }
        if (description != null) {
            Element e = doc.createElement( "desc" );
            e.appendChild( doc.createTextNode( description ));
            root.appendChild( e );
        }
        if (helpUrl != null) {
            root.setAttributeNS( SynopticConfig.DISPLAY_NS, "syndi:help-url", helpUrl );
        }
        for (ComponentProperty prop : getProperties()) {
            if (!prop.isGlobal()) {
                continue;
            }
            Element e = prop.getXML_NS( doc );
            root.appendChild( e );
        }
        root.appendChild( svg.getXML( doc ));
        return root;
    }
    
    @Override
    public void setParent( BuilderContainer parent ) {
        this.parent = parent;
    }

    @Override
    public BuilderContainer getParent() {
        return parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName( String name ) {
        this.name = name;
    }

    @Override
    public String getCaption() {
        return getName();
    }

    @Override
    public ComponentType getType() {
        return ComponentType.GRAPHICS;
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
    
    @Override
    public void setSelected( boolean val ) {
        selected = val;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean contains( Point p ) {
        return p.x >= x + bounds.x - HIT_TOLERANCE &&
               p.x <  x + bounds.x + bounds.width + HIT_TOLERANCE &&
               p.y >= y + bounds.y - HIT_TOLERANCE &&
               p.y <  y + bounds.y + bounds.height + HIT_TOLERANCE;
    }

    @Override
    public void setLocation( int x, int y ) {
        if (x == this.x && y == this.y) {
            return;
        }
        Point p0 = new Point( this.x, this.y );
        this.x = x;
        this.y = y;
        recalculateBounds();
        Point p1 = new Point( x, y );
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

    public int getWidth() {
        return bounds.width;
    }

    public int getHeight() {
        return bounds.height;
    }

    public void setBounds( Rectangle bounds ) {
        Rectangle b = new Rectangle( bounds );
        b.x -= x;
        b.y -= y;
        if (b.width < MIN_SIZE) {
            b.width = MIN_SIZE;
        }
        if (b.height < MIN_SIZE) {
            b.height = MIN_SIZE;
        }
        if (this.bounds.equals( b )) {
            return;
        }
        Dimension d0 = new Dimension( this.bounds.width, this.bounds.height );
        try {
            svg.setBounds( b );
        } catch (NoninvertibleTransformException ex) {
            log.warning( "Cannot change bounds of " + this );
        }
        recalculateBounds();
        Dimension d1 = new Dimension( this.bounds.width, this.bounds.height );
        firePropertyChange( "size", d0, d1 );
    }
    
    @Override
    public Rectangle getBounds() {
        return new Rectangle(
            bounds.x + x,
            bounds.y + y,
            bounds.width,
            bounds.height
        );
    }

    public void setTransform( AffineTransform xform ) {
        Shape s0 = svg.getOutline();
        svg.setTransform( xform );
        recalculateBounds();
        Shape s1 = svg.getOutline();
        firePropertyChange( "shape", s0, s1 );
    }

    public AffineTransform getTransform() {
        return svg.getTransform();
    }

    @Override
    public abstract Shape getMovingShape( Point dp );
    
    @Override
    public void paint( Graphics2D g ) {
        // Here, the graphics object is already transformer to (X,Y) coordinate
        cachedGridAttributes = getGridAttributes();
        svg.paint( g );
        if (isSelected()) {
            g.setColor( ANCHOR_COLOR );
            g.setStroke( BASE_STROKE );
            for (Point p : getAnchors()) {
                g.fill( createAnchorShape( p ));
            }
        }
        if (outline != null) {
            AffineTransform xform = g.getTransform();
            float zoom = (xform == null) ? 1.0f : (float)xform.getScaleX();
            g.setStroke( new BasicStroke( 1.0f / zoom ));
            g.setColor( OUTLINE_COLOR );
            lastOutlineBounds = outline.getBounds();
            g.draw( outline );
            outline = null;
        } else {
            lastOutlineBounds = null;
        }
    }
    
    protected void repaintComponent( int x, int y, int w, int h ) { // Relatively to this component
        if (parent != null) {
            parent.repaintComponent( this.x + x, this.y + y, w, h );
        }
    }

    protected void paintOutline( Shape outline ) {
        this.outline = outline;
        Rectangle dirtyRegion = lastOutlineBounds;
        if (dirtyRegion != null) {
            dirtyRegion.add( outline.getBounds());
        } else {
            dirtyRegion = outline.getBounds();
        }
        repaintComponent(
            dirtyRegion.x,
            dirtyRegion.y,
            dirtyRegion.width + 1,
            dirtyRegion.height + 1
        );
    }
    
    protected abstract Point[] getAnchors();
    
    @Override
    public void repaintComponent() {
        repaintComponent( 
            bounds.x - HIT_TOLERANCE,
            bounds.x - HIT_TOLERANCE,
            bounds.width + 2 * HIT_TOLERANCE,
            bounds.height + 2 * HIT_TOLERANCE
        );
    }

    @Override
    public void reload() {}

    protected Shape createAnchorShape( Point p ) {
        int r = ANCHOR_SIZE / 2;
        return new Rectangle( p.x - r, p.y - r, r * 2 + 1, r * 2 + 1 );
    }

    public void doAction( CanvasAction action ) {
        if (parent != null) {
            parent.doAction( action );
        }
    }
    
    public void setCursor( Cursor val ) {
        if (parent != null) {
            parent.setCursor( val );
        }
    }
    
    public GridAttributes getGridAttributes() {
        return (parent != null) ? parent.getGridAttributes() : cachedGridAttributes;
    }
    
    protected int snap( int val ) {
        GridAttributes attr = cachedGridAttributes;
        if (attr.isEnabled()) {
            int step = attr.getStep();
            return (val >= 0)
                ? (val / step) * step
                : ((val + 1) / step - 1) * step;
        } else {
            return val;
        }
    }

    protected Point snap( Point p ) {
        return new Point( snap( p.x ), snap( p.y ));
    }
    
    @Override
    public String toString() {
        return getName();
    }

    protected void firePropertyChange( String name, Object oldValue, Object newValue ) {
        if (!(parent instanceof PropertyChangeListener)) {
            return;
        }
        PropertyChangeEvent e = new PropertyChangeEvent( this, name, oldValue, newValue );
        ((PropertyChangeListener)parent).propertyChange( e );
    }

    @Override
    public void setProperties( Collection<ComponentProperty<?>> val ) throws PropertyException {
        props.addAll( val );
    }

    protected void recalculateBounds() {
        Rectangle2D r2 = svg.getBounds();
        if (r2 != null) {
            bounds = r2.getBounds();
        } else {
            bounds = new Rectangle();
        }
    }

}
