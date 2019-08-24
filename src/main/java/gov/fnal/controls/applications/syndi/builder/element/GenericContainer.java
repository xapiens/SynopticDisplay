// (c) 2001-2010 Fermi Research Allaince
// $Id: GenericContainer.java,v 1.4 2010/09/20 21:54:07 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.DrawingCanvas;
import gov.fnal.controls.applications.syndi.builder.element.pin.Pin;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinAddress;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinRole;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.PropertyList;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/20 21:54:07 $
 */
public class GenericContainer extends GenericComponent implements BuilderContainer, PropertyChangeListener {
    
    private static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
    private static final Cursor MOVE_CURSOR = Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR );
    private static final Cursor CROSSHAIR_CURSOR = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR );
    
    public enum State {
        IDLE, SELECT, MOVE, PLACE_COMPONENT, PLACE_LINE;
    }

    // Components are painted from the top to the bottom  
    private final List<BuilderComponent> components = new ArrayList<BuilderComponent>();
    private final List<BuilderComponent> candidates = new ArrayList<BuilderComponent>();
    private final List<BuilderComponent> selection  = new ArrayList<BuilderComponent>();
    private final SortedSet<Integer> ids = new TreeSet<Integer>();

    private State state = State.IDLE;
    private BuilderComponent lastComp = null;
    private Point origin = null;
    private Rectangle minBounds = null;
    private int lastLinkIndex = -1;
    
    private EnumSet<ComponentType> cachedVisibleTypes = EnumSet.allOf( ComponentType.class );

    public GenericContainer() {
        ids.add( 0 );
    }

    public boolean isRoot() {
        return getParent() instanceof DrawingCanvas;
    }

    @Override
    public String getName() {
        return isRoot() ? "Display" : "Nested Display";
    }

    @Override
    public Integer getId() {
        return isRoot() ? null : super.getId();
    }
    
    public State getState() {
        return state;
    }

    @Override
    public boolean isBackgroundImageEnabled() {
        return isRoot();
    }

    @Override
    public void setLocation( int x, int y ) {
        if (isRoot()) {
            for (BuilderComponent c : components) {
                c.setLocation( c.getX() - x, c.getY() - y );
            }
        } else {
            super.setLocation( x, y );
        }
    }
    
    @Override
    protected void paintContents( Graphics2D g ) {
        cachedVisibleTypes = getVisibleTypes();
        if (isRoot()) {
            GridAttributes attrs = cachedGridAttributes;
            if (attrs.isVisible()) {
                int step = attrs.getStep();
                g.setColor( GRID_COLOR );
                for (int x = step; x < getWidth(); x += step) {
                    for (int y = step; y < getHeight(); y += step) {
                        g.fillRect( x, y, 1, 1 );
                    }
                }
            }
        }
        List<BuilderComponent> visibleSelection = new ArrayList<BuilderComponent>( selection );
        boolean changed = false;
        for (Iterator<BuilderComponent> z = visibleSelection.iterator(); z.hasNext();) {
            if (!isVisible( z.next())) {
                z.remove();
                changed = true;
            }
        }
        if (changed) {
            select( visibleSelection );
        }
        if (lastComp != null && !isVisible( lastComp )) {
            lastComp = null;
        }
        for (BuilderComponent c : components) {
            if (!isVisible( c )) {
                continue;
            }
            g.translate( c.getX(), c.getY());
            c.paint( g );
            g.translate( -c.getX(), -c.getY());
        }
    }
    
    @Override
    public EnumSet<ComponentType> getVisibleTypes() {
        BuilderContainer parent = getParent();
        return (parent != null) 
                ? EnumSet.copyOf( parent.getVisibleTypes()) 
                : EnumSet.allOf( ComponentType.class );
    }
    
    private boolean isVisible( BuilderComponent comp ) {
        ComponentType t = comp.getType();
        return t == null || cachedVisibleTypes.contains( t );
    }

    @Override
    public boolean add( BuilderComponent comp ) {
        return add( comp, components.size());
    }

    public boolean add( BuilderComponent comp, int index ) {
        if (components.contains( comp )) {
            throw new IllegalArgumentException( "Ingredient already added" );
        }
        if (index < 0) {
            index = 0;
        }
        if (index > components.size()) {
            index = components.size();
        }
        BuilderContainer parent = comp.getParent();
        if (parent instanceof GenericContainer) {
            ((GenericContainer)parent).remove( comp );
        } else if (parent != null) {
            throw new IllegalArgumentException( "Invalid previous parent" );
        }
        Integer id = null;
        if (comp instanceof AbstractComponent) {
            AbstractComponent c = (AbstractComponent)comp;
            id = c.getId();
            if (id == null) {
                id = ids.last() + 1;
                c.setId( id );
            } else if (ids.contains( id )) {
                throw new IllegalArgumentException( "Duplicated component ID" );
            }
        }
        comp.setParent( this );
        if (id != null) {
            ids.add( id );
        }
        if (comp instanceof ComponentLink) {
            index = Math.min( index, ++lastLinkIndex );
        } else {
            index = Math.max( index, lastLinkIndex + 1 );
        }
        components.add( index, comp );
        recalculateBounds();
        return true;
    }

    public void remove( BuilderComponent comp ) {
        remove_( comp );
        recalculateBounds();
    }
    
    private void remove_( BuilderComponent comp ) {
        if (!components.contains( comp )) {
            return;
        }
        components.remove( comp );
        comp.setParent( null );
        if (comp instanceof ComponentLink) {
            lastLinkIndex--;
        } else if (comp instanceof AbstractComponent) {
            AbstractComponent c = (AbstractComponent)comp;
            if (c.getId() != null) {
                ids.remove( c.getId());
            }
            for (ComponentLink link : c.links()) {
                remove_( link );
            }
        }
    }

    public void move( int fromIndex, int toIndex ) {
        BuilderComponent comp = components.remove( fromIndex );
        components.add( toIndex, comp );
    }
    
    public BuilderComponent getComponentAt( Point p ) {
        ListIterator<BuilderComponent> z = components.listIterator( components.size());
        while (z.hasPrevious()) {
            BuilderComponent c = z.previous();
            if (c.contains( p ) && isVisible( c )) {
                return c;
            }
        }
        return null;
    }

    public List<BuilderComponent> getComponentsAt( Point p ) {
        List<BuilderComponent> res = new LinkedList<BuilderComponent>();
        ListIterator<BuilderComponent> z = components.listIterator( components.size());
        while (z.hasPrevious()) {
            BuilderComponent c = z.previous();
            if (c.contains( p ) && isVisible( c )) {
                res.add( c );
            }
        }
        res.add( this );
        return res;
    }
    
    public AbstractComponent getComponentById( int id ) {
        if (id == 0) {
            return this;
        }
        for (BuilderComponent c : components) {
            if ((c instanceof AbstractComponent) && ((AbstractComponent)c).getId() == id
                    && isVisible( c )) {
                return (AbstractComponent)c;
            }
        }
        return null;
    }
    
    public int getComponentCount() {
        return components.size();
    }
    
    public int indexOf( BuilderComponent comp ) {
        return components.indexOf( comp );
    }
    
    @Override
    public Iterator<BuilderComponent> iterator() {
        return new BuilderComponentIterator();
    }
    
    private BuilderComponent getSelectionAt( Point p ) {
        for (BuilderComponent c : selection) {
            if (c != this && c.contains( p )) {
                return c;
            }
        }
        return null;
    }

    private void recalculateBounds() {
        Rectangle res = null;
        for (BuilderComponent c : components) {
            if (c instanceof ComponentLink) {
                continue;
            }
            Rectangle r = c.getBounds();
            if (res == null) {
                res = r;
            } else {
                res.add( r );
            }
        }
        minBounds = res;
    }
    
    @Override
    protected Rectangle getResizeOutline( Point p, Anchor anchor ) {
        Rectangle r = super.getResizeOutline( p, anchor );
        if (isRoot() && minBounds != null) {
            int x0 = Math.min( r.x, minBounds.x );
            int y0 = Math.min( r.y, minBounds.y );
            int x1 = Math.max( r.x + r.width, minBounds.x + minBounds.width );
            int y1 = Math.max( r.y + r.height, minBounds.y + minBounds.height );
            return new Rectangle( x0, y0, x1 - x0, y1 - y0 ); 
        } else {
            return r;
        }
    }

    private Rectangle getSelectedArea( Point p ) {
        int x = origin.x;
        int y = origin.y;
        int w = p.x - x - 1;
        if (w < 0) {
            w = -w;
            x -= w;
        }
        int h = p.y - y - 1;
        if (h < 0) {
            h = -h;
            y -= h;
        }
        return new Rectangle( x, y, w, h );
    }

    private Path2D getCombinedOutline( Point dp, Collection<BuilderComponent> comp ) {
        Path2D res = new GeneralPath();
        for (BuilderComponent c : comp) {
            if (c == this) {
                continue;
            }
            Shape s = c.getMovingShape( dp );
            if (s != null) {
                res.append( s, false );
            }
        }
        return res;
    }

    private Point getMovingShift( Point p0, Point p1, Collection<BuilderComponent> comp ) {
        if (comp.isEmpty()) {
            return new Point( p1.x - p0.x, p1.y - p0.y );
        }
        int xx0 = Integer.MAX_VALUE, yy0 = Integer.MAX_VALUE;
        int xx1 = Integer.MIN_VALUE, yy1 = Integer.MIN_VALUE;
        for (BuilderComponent c : comp) {
            Rectangle r = c.getBounds();
            xx0 = Math.min( xx0, r.x );
            xx1 = Math.max( xx1, r.x + r.width );
            yy0 = Math.min( yy0, r.y );
            yy1 = Math.max( yy1, r.y + r.height );
        }
        int dx = p1.x - p0.x;
        if (dx < 0) {
            dx = snap( xx0 + Math.max( dx, -xx0 )) - xx0;
        } else if (dx > 0) {
            dx = snap( xx1 + Math.min( dx, getWidth() - xx1 )) - xx1; 
        }
        int dy = p1.y - p0.y;
        if (dy < 0) {
            dy = snap( yy0 + Math.max( dy, -yy0 )) - yy0;
        } else if (dy > 0) {
            dy = snap( yy1 + Math.min( dy, getHeight() - yy1 )) - yy1; 
        }
        return new Point( dx, dy );
    }
    
    private void grabUnderMouse( MouseEvent e ) {
        BuilderComponent comp = getSelectionAt( e.getPoint());
        if (comp != lastComp) {
            if (lastComp instanceof MouseListener) {
                ((MouseListener)lastComp).mouseExited( new MouseEvent(
                    e.getComponent(),
                    e.getID(),
                    e.getWhen(),
                    e.getModifiers(),
                    e.getX(),
                    e.getY(),
                    e.getClickCount(),
                    e.isPopupTrigger()
                ));
            }
            if (comp instanceof MouseListener) {
                ((MouseListener)comp).mouseEntered( new MouseEvent(
                    e.getComponent(),
                    e.getID(),
                    e.getWhen(),
                    e.getModifiers(),
                    e.getX(),
                    e.getY(),
                    e.getClickCount(),
                    e.isPopupTrigger()
                ));
            }
            lastComp = comp;
        }
    }
    
    private Pin pinToConnect( Point p, ComponentLink link ) {
        BuilderComponent c = getComponentAt( p );
        if (c == null) {
            c = this;
        }
        if (c instanceof AbstractComponent) {
            AbstractComponent comp = (AbstractComponent)c;
            Pin pin = comp.getPinAt( p.x - c.getX(), p.y - c.getY());
            if (pin == null) {
                return null;
            }
            PinRole role = pin.roleFor( this );
            if (pin.getLink( role ) == null && link.getPinAddress( role ) == null &&
                    (role == PinRole.SOURCE || link.getPinAddress( PinRole.SOURCE ) != null)) {
                p.setLocation( pin.getLocationOnComponent());
                p.translate( c.getX(), c.getY());
                return pin;
            }
        }
        return null;
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        if (state == State.IDLE) {
            super.mouseMoved( e );
            if (e.isConsumed() || !isRoot()) {
                return;
            }
            grabUnderMouse( e );
            if (lastComp instanceof MouseMotionListener) {
                e.translatePoint( -lastComp.getX(), -lastComp.getY());
                ((MouseMotionListener)lastComp).mouseMoved( e );
                e.translatePoint( lastComp.getX(), lastComp.getY());
                if (e.isConsumed()) {
                    return;
                }
            }
        }
        switch (state) {
            case PLACE_COMPONENT :
                e.consume();
                Point dp = getMovingShift( new Point( 0, 0 ), e.getPoint(), candidates );
                paintOutline( getCombinedOutline( dp, candidates ));
                break;
            case PLACE_LINE :
                e.consume();
                Point p = e.getPoint();
                Pin pin = null;
                GenericLine line = (GenericLine)candidates.get( 0 );
                if (line instanceof ComponentLink) {
                    pin = pinToConnect( p, (ComponentLink)line );
                }
                if (pin == null) {
                    p = snap( p );
                }
                Path2D outline = line.getSegmentMovingShape( p );
                if (outline == null) {
                    outline = new GeneralPath();
                }
                if (pin != null) {
                    int r = PIN_SIZE;
                    Shape s = new Rectangle( p.x - r, p.y - r, 2 * r, 2 * r );
                    outline.append( s, false );
                }
                paintOutline( outline );
                break;
        }
    }
    
    @Override
    public void mouseDragged( MouseEvent e ) {
        if (state == State.IDLE) {
            super.mouseDragged( e );
            if (e.isConsumed() || !isRoot()) {
                return;
            }
            if (lastComp == null) {
                state = State.SELECT;
            } else {
                if (lastComp instanceof MouseMotionListener) {
                    e.translatePoint( -lastComp.getX(), -lastComp.getY());
                    ((MouseMotionListener)lastComp).mouseDragged( e );
                    e.translatePoint( lastComp.getX(), lastComp.getY());
                    if (e.isConsumed()) {
                        return;
                    }
                }
                state = State.MOVE;
                setCursor( MOVE_CURSOR );
            }
            origin = new Point( e.getPoint());
        }
        switch (state) {
            case SELECT :
                e.consume();
                paintOutline( getSelectedArea( e.getPoint()));
                break;
            case MOVE :
                e.consume();
                Point dp = getMovingShift( origin, e.getPoint(), selection );
                paintOutline( getCombinedOutline( dp, getInnerSelection( false )));
                break;
        }
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
        if (state == State.IDLE) {
            super.mouseClicked( e );
            if (e.isConsumed() || !isRoot()) {
                return;
            }
            BuilderComponent c = getComponentAt( e.getPoint());
            if (c == null) {
                c = this;
            } else if (c instanceof MouseListener) {
                e.translatePoint( -c.getX(), -c.getY());
                ((MouseListener)c).mouseClicked( e );
                e.translatePoint( c.getX(), c.getY());
                if (e.isConsumed()) {
                    return;
                }
            }
        }
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        Point p;
        switch (state) {
            case PLACE_COMPONENT :
                e.consume();
                p = getMovingShift( new Point( 0, 0 ), e.getPoint(), candidates    );
                for (BuilderComponent c : candidates) {
                    c.setLocation( c.getX() + p.x, c.getY() + p.y );
                }
                doAction( new PlaceComponentAction( this, candidates ));
                state = State.IDLE;
                firePropertyChange( "placing", true, false );
                setSelection( candidates, true );
                setCursor( DEFAULT_CURSOR );
                repaintComponent();
                break;
            case PLACE_LINE :
                e.consume();
                p = e.getPoint();
                Pin pin = null;
                boolean connectionDone = false;
                GenericLine line = (GenericLine)candidates.get( 0 );
                if (line instanceof ComponentLink) {
                    ComponentLink link = (ComponentLink)line;
                    boolean hasSource = link.getPinAddress( PinRole.SOURCE ) != null;
                    boolean hasTarget = link.getPinAddress( PinRole.TARGET ) != null;
                    pin = pinToConnect( p, link );
                    if (pin != null) {
                        PinRole role = pin.roleFor( this );
                        if (role == PinRole.SOURCE && !hasSource && !hasTarget ||
                                role == PinRole.TARGET && hasSource && !hasTarget) {
                            link.setPinAddress( role, pin.getAddress( role ));
                        } else {
                            return;
                        }
                    } else if (!hasSource) {
                        return;
                    }
                    connectionDone = link.isConnected();
                } else {
                    connectionDone = e.getClickCount() > 1;
                }
                if (pin == null) {
                    p = snap( p );
                } 
                line.placeNewPoint( p.x, p.y );
                if (connectionDone) {
                    doAction( new PlaceComponentAction( this, candidates ));
                    state = State.IDLE;
                    firePropertyChange( "placing", true, false );
                    setSelection( line, true );
                    setCursor( DEFAULT_CURSOR );
                    repaintComponent();
                }
                break;
        }

    }

    @Override
    public void mousePressed( MouseEvent e ) {
        if (state == State.IDLE) {
            super.mousePressed( e );
            if (e.isConsumed() || !isRoot()) {
                return;
            }
            BuilderComponent c = getComponentAt( e.getPoint());
            if (c == null) {
                c = this;
            } else if (c instanceof MouseListener) {
                e.translatePoint( -c.getX(), -c.getY());
                ((MouseListener)c).mousePressed( e );
                e.translatePoint( c.getX(), c.getY());
                if (e.isConsumed()) {
                    return;
                }
            }
            if (c.isSelected()) {
                return;
            }
            if (SwingUtilities.isLeftMouseButton( e ) || SwingUtilities.isRightMouseButton( e )) {
                setSelection( c, (e.getModifiers() & MouseEvent.CTRL_MASK) == 0 );
            }
        }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        if (state == State.IDLE) {
            super.mouseReleased( e );
            if (e.isConsumed() || !isRoot()) {
                return;
            }
            if (lastComp instanceof MouseListener) {
                e.translatePoint( -lastComp.getX(), -lastComp.getY());
                ((MouseListener)lastComp).mouseReleased( e );
                e.translatePoint( lastComp.getX(), lastComp.getY());
                if (e.isConsumed()) {
                    return;
                }
            }
            if (SwingUtilities.isLeftMouseButton( e ) || SwingUtilities.isRightMouseButton( e )) {
                grabUnderMouse( e );
            }
        }
        switch (state) {
            case MOVE :
                e.consume();
                Point dp = getMovingShift( origin, e.getPoint(), selection  );
                state = State.IDLE;
                setCursor( DEFAULT_CURSOR );
                doAction( new MoveComponentAction( this, getInnerSelection( false ), dp ));
                break;
            case SELECT :
                e.consume();
                state = State.IDLE;
                setCursor( DEFAULT_CURSOR );
                setSelection( getSelectedArea( e.getPoint()), (e.getModifiers() & MouseEvent.CTRL_MASK) == 0 );
                break;
        }
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        super.mouseExited( e );
        if (lastComp instanceof MouseListener) {
            ((MouseListener)lastComp).mouseExited( new MouseEvent(
                e.getComponent(),
                e.getID(),
                e.getWhen(),
                e.getModifiers(),
                e.getX() - lastComp.getX(),
                e.getY() - lastComp.getY(),
                e.getClickCount(),
                e.isPopupTrigger()
            ));
        }
        lastComp = null;
    }
    
    public void place( List<BuilderComponent> comp ) {
        boolean was_placing = (state == State.PLACE_COMPONENT || state == State.PLACE_LINE);
        candidates.clear();
        int x0 = Integer.MAX_VALUE;
        int y0 = Integer.MAX_VALUE;
        if (comp != null) {
            for (int i = 0; i < 2; i++) {
                for (BuilderComponent c : comp) {
                    if (((c instanceof ComponentLink) ^ (i == 0)) && isVisible( c )) {
                        candidates.add( c );
                        Rectangle r = c.getBounds();
                        x0 = Math.min( x0, r.x );
                        y0 = Math.min( y0, r.y );
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            if (!was_placing) {
                return;
            }
            state = State.IDLE;
            setCursor( DEFAULT_CURSOR );
            repaintComponent();
            firePropertyChange( "placing", true, false );
            return;
        }
        renumerate( candidates, ids.last() + 1 );
        if ((candidates.size() == 1) && isEmptyLine( candidates.get( 0 ))) {
            state = State.PLACE_LINE;
            setCursor( CROSSHAIR_CURSOR );
        } else {
            state = State.PLACE_COMPONENT;
            setCursor( MOVE_CURSOR );
        }
        for (BuilderComponent c : candidates) {
            c.setLocation( c.getX() - x0, c.getY() - y0 );
        }
        firePropertyChange( "placing", was_placing, true );
    }

    private void renumerate( List<BuilderComponent> comp, int startId ) {
        int newId = startId;
        Map<Integer,Integer> map = new HashMap<Integer,Integer>(); // oldId -> newId
        for (BuilderComponent c : comp) {
            if (!(c instanceof AbstractComponent)) {
                continue;
            }
            AbstractComponent ac = (AbstractComponent)c;
            Integer oldId = ac.getId();
            if (oldId == null) {
                continue;
            }
            map.put( oldId, newId );
            ac.setId( newId );
            ++newId;
        }
        for (Iterator<BuilderComponent> z = comp.iterator(); z.hasNext(); ) {
            BuilderComponent c = z.next();
            if (!(c instanceof ComponentLink)) {
                continue;
            }
            ComponentLink cl = (ComponentLink)c;
            cl.setParent( null ); // reset pin information
            PinAddress srcPin = cl.getPinAddress( PinRole.SOURCE );
            if (srcPin != null) {
                Integer srcId = map.get( srcPin.getComponentId());
                if (srcId == null) {
                    z.remove();
                    continue;
                }
                cl.setPinAddress( PinRole.SOURCE, new PinAddress( srcId, srcPin.getPinId()));
            }
            PinAddress tgtPin = cl.getPinAddress( PinRole.TARGET );
            if (tgtPin != null) {
                Integer tgtId = map.get( tgtPin.getComponentId());
                if (tgtId == null) {
                    z.remove();
                    continue;
                }
                cl.setPinAddress( PinRole.TARGET, new PinAddress( tgtId, tgtPin.getPinId()));
            }
        }

    }

    private static boolean isEmptyLine( BuilderComponent c ) {
        if (!(c instanceof GenericLine)) {
            return false;
        }
        Point[] anchors = ((GenericLine)c).getAnchors();
        return (anchors == null) || anchors.length == 0; // TODO
    }

    public List<BuilderComponent> getSelection() {
        return new ArrayList<BuilderComponent>( selection );
    }

    public void selectNone() {
        doAction( new SelectComponentAction( this, null ));
    }
    
    public void selectAll() {
        doAction( new SelectComponentAction( this, components ));
    }

    public boolean select( int compId ) {
        for (BuilderComponent c : components) {
            if (!(c instanceof AbstractComponent)) {
                continue;
            }
            Integer id = ((AbstractComponent)c).getId();
            if (id != null && id.intValue() == compId) {
                setSelection( c, true );
                return true;
            }
        }
        return false;
    }

    public boolean select( BuilderComponent comp ) {
        if (comp != this && comp.getParent() != this) {
            return false;
        }
        setSelection( comp, true );
        return true;
    }

    public void deleteSelection() {
        doAction( new DeleteComponentAction( this, getInnerSelection( true )));
    }

    private List<BuilderComponent> getInnerSelection( boolean includeOrphantLinks ) {
        List<BuilderComponent> res = new ArrayList<BuilderComponent>( selection );
        res.remove( this );
        if (!includeOrphantLinks) {
            for (Iterator<BuilderComponent> z = res.iterator(); z.hasNext();) {
                BuilderComponent comp = z.next();
                if (!(comp instanceof ComponentLink)) {
                    continue;
                }
                ComponentLink link = (ComponentLink)comp;
                Pin srcPin = link.getPin( PinRole.SOURCE );
                Pin tgtPin = link.getPin( PinRole.TARGET );
                AbstractComponent srcComp = (srcPin == null) ? null : srcPin.getComponent();
                AbstractComponent tgtComp = (tgtPin == null) ? null : tgtPin.getComponent();
                if ((srcComp != null && !selection.contains( srcComp ))
                        || (tgtComp != null && !selection.contains( tgtComp ))) {
                    z.remove();
                }
            }
        }
        return res;
    }
    
    private void setSelection( BuilderComponent comp, boolean exclusive ) {
        setSelection( Arrays.asList( comp ), exclusive );
    }

    private void setSelection( Rectangle r, boolean exclusive ) {
        List<BuilderComponent> sel = new ArrayList<BuilderComponent>();
        for (BuilderComponent c : components) {
            Rectangle bounds = c.getBounds();
            if (bounds.width == 0) {
                bounds.width = 1;
            }
            if (bounds.height == 0) {
                bounds.height = 1;
            }
            if (r.contains( bounds )) {
                sel.add( c );
            }
        }
        setSelection( sel, exclusive );
    }

    private void setSelection( List<BuilderComponent> comps, boolean exclusive ) {
        List<BuilderComponent> sel = new ArrayList<BuilderComponent>();
        if (!exclusive) {
            sel.addAll( selection );
        }
        sel.addAll( comps );
        doAction( new SelectComponentAction( this, sel ));
    }
    
    protected void select( List<BuilderComponent> sel ) {
        BuilderComponent[] s0 = toArray( selection );
        BuilderComponent[] s1 = toArray( sel );
        selection.removeAll( sel );
        for (BuilderComponent c : selection) {
            c.setSelected( false );
        }
        selection.clear();
        selection.addAll( sel );
        for (BuilderComponent c : selection) {
            c.setSelected( true );
        }
        firePropertyChange( "selection", s0, s1 );
    }

    public void changeOrder( BuilderComponent comp, int dir ) {
        doAction( new ChangeOrderAction( comp, dir ));
    }

    @Override
    public Element getXML( Document doc ) {
        Element res = super.getXML( doc );
        for (BuilderComponent c : components) {
            if (!(c instanceof ComponentLink)) {
                res.appendChild( c.getXML( doc ));
            }
        }
        for (BuilderComponent c : components) {
            if (c instanceof ComponentLink) {
                res.appendChild( c.getXML( doc ));
            }
        }
        return res;
    }

    @Override
    public void propertyChange( PropertyChangeEvent e ) {
        String pName = e.getPropertyName();
        if ("location".equals( pName ) || "size".equals( pName )
                || "shape".equals( pName )) {
            recalculateBounds();
        }
        BuilderContainer parent = getParent();
        if (parent instanceof PropertyChangeListener) {
            ((PropertyChangeListener)parent).propertyChange( e );
        }
    }
    
    private static BuilderComponent[] toArray( Collection<BuilderComponent> val ) {
        return val.toArray( new BuilderComponent[ val.size()]);
    }

    @Override
    public void setGlobalProperties( Collection<ComponentProperty<?>> val ) throws PropertyException {
        for (ComponentProperty<?> p : val) {
            Object c = p.getComponent();
            if (!(c instanceof BuilderComponent)) {
                continue;
            }
            BuilderComponent bc = (BuilderComponent)c;
            Collection<ComponentProperty<?>> cp = bc.getProperties();
            cp.add( p );
            bc.setProperties( cp );
        }
    }

    @Override
    public Collection<ComponentProperty<?>> getGlobalProperties() {
        PropertyList list = new PropertyList();
        appendGlobalProperties( this, list );
        for (BuilderComponent comp : this) {
            appendGlobalProperties( comp, list );
        }
        return list;
    }

    @Override
    public void reload() {
        for (BuilderComponent c : components) {
            c.reload();
        }
    }

    private static void appendGlobalProperties( BuilderComponent comp, PropertyList list ) {
        for (ComponentProperty<?> p : comp.getProperties()) {
            if (p.isGlobal()) {
                list.add( p );
            }
        }
    }
    
    private class BuilderComponentIterator implements Iterator<BuilderComponent> {
        
        private final Iterator<BuilderComponent> z = components.iterator();

        @Override
        public boolean hasNext() {
            return z.hasNext();
        }

        @Override
        public BuilderComponent next() {
            return z.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}

