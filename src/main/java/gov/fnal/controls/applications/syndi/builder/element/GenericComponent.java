// (c) 2001-2010 Fermi Research Allaince
// $Id: GenericComponent.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.builder.CanvasAction;
import gov.fnal.controls.applications.syndi.builder.element.pin.Pin;
import gov.fnal.controls.applications.syndi.builder.element.pin.PinType;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class GenericComponent extends AbstractComponent 
        implements MouseListener, MouseMotionListener {
    
    private static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
    private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor( Cursor.HAND_CURSOR );

    private static final Stroke BORDER_STROKE = new BasicStroke( (float)BORDER_SIZE );
    private static final Stroke BASE_STROKE = new BasicStroke();

    private Anchor currentAnchor = null;
    private Pin currentPin = null;
    private Shape outline = null;
    private Rectangle outlineBounds = null;
    
    protected GridAttributes cachedGridAttributes = new GridAttributes();
    
    public GenericComponent() {}
    
    @Override
    public synchronized void paint( Graphics2D g ) {
        
        cachedGridAttributes = getGridAttributes();

        Shape clip = g.getClip();
        g.clipRect( 1, 1, getWidth() - 2, getHeight() - 2 );
        paintContentsWithBackground( g );
        g.setClip( clip );

        paintBorder( g );
        
        g.setStroke( BASE_STROKE );
        
        if (isSelected()) {
            g.setColor( ANCHOR_COLOR );
            for (Anchor a : Anchor.values()) {
                Shape s = createAnchorShape( a.getLocation());
                g.fill( s );
            }
        }

        g.setStroke( BASE_STROKE );

        BuilderContainer parent = getParent();
        EnumSet<ComponentType> visibleTypes = (parent != null) 
                ? parent.getVisibleTypes()
                : EnumSet.allOf( ComponentType.class );
        
        if (visibleTypes.contains( ComponentType.LINK )) {
            for (Pin pin : pins) {
                Shape s = createPinShape( pin.getLocation());
                g.setColor( pin.getType() == PinType.INPUT ? INPUT_PIN_COLOR : OUTPUT_PIN_COLOR );
                g.fill( s );
                g.setColor( PIN_BORDER_COLOR );
                g.draw( s );
                paintPinCaption( g, pin );
            }
        }
        
        if (outline != null) {
            AffineTransform xform = g.getTransform();
            float zoom = (xform == null) ? 1.0f : (float)xform.getScaleX();
            g.setStroke( new BasicStroke( 1.0f / zoom ));
            outlineBounds = outline.getBounds();
            g.setColor( OUTLINE_COLOR );
            g.draw( outline );
            outline = null;
        } else {
            outlineBounds = null;
        }
        
    }
    
    protected void paintBorder( Graphics2D g ) {
        g.setColor( BORDER_COLOR );
        g.setStroke( BORDER_STROKE );
        g.drawRect( 
            BORDER_SIZE / 2, 
            BORDER_SIZE / 2, 
            getWidth() - BORDER_SIZE, 
            getHeight() - BORDER_SIZE 
        );
    }
    
    protected synchronized void paintOutline( Shape outline ) {
        Rectangle r0 = outline.getBounds();
        Rectangle r1 = this.outlineBounds;
        if (r1 != null) {
            r0.add( r1 );
        }
        this.outline = outline;
        repaintComponent( r0.x, r0.y, r0.width + 1, r0.height + 1 );
    }
    
    protected void paintContentsWithBackground( Graphics2D g ) {
        Color bgColor = getBackgroundColor();
        if (bgColor != null) {
            g.setColor( bgColor );
            g.fillRect( 0, 0, getWidth(), getHeight());
        }
        Image bgImage = getBackgroundImage();
        if (bgImage != null) {
            g.drawImage( bgImage, 0, 0, getWidth(), getHeight(), bgColor, null );
        }
        paintContents( g );
    }

    protected void paintContents( Graphics2D g ) {
        String str = getCaption();
        if (str == null) {
            return;
        }
        Rectangle bounds = new Rectangle(
            ANCHOR_SIZE / 2,
            ANCHOR_SIZE / 2,
            getWidth() - ANCHOR_SIZE,
            getHeight() - ANCHOR_SIZE
        );
        g.setFont( CAPTION_FONT );
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D r = fm.getStringBounds( str, g );
        if (r.getHeight() > bounds.height) {
            return;
        }
        double x = bounds.x + (bounds.width - r.getWidth()) / 2;
        double y = bounds.y - r.getY() + (bounds.height - r.getHeight()) / 2;
        g.setColor( COMPONENT_CAPTION_COLOR );
        g.drawString( str, Math.round( x ), Math.round( y ));
    }

    protected String getPinCaption( Pin pin ) {
        String str = pin.getName();
        if (str != null) {
            return str;
        }
        if (pin.getType() == PinType.INPUT && pins.getMaxInputCount() > 1) {
            return String.valueOf( pin.getIndex());
        }
        return null;
    }
    
    protected void paintPinCaption( Graphics2D g, Pin pin ) {
        String str = getPinCaption( pin );
        if (str == null) {
            return;
        }
        g.setFont( CAPTION_FONT );
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D r = fm.getStringBounds( str, g );
        if (r.getHeight() > getHeight() - ANCHOR_SIZE) {
            return;
        }
        Point q = pin.getLocationOnComponent();
        double x = q.getX();
        double y = q.getY() - r.getY();
        if (pin.getX() == 0.0f) {
            x += PIN_SIZE / 2.0 + PIN_CAPTION_SPACING;
        } else if (pin.getX() == 1.0f) {
            x -= PIN_SIZE / 2.0 + r.getWidth() + PIN_CAPTION_SPACING;
        } else {
            x -= r.getWidth() / 2.0;
        }
        if (pin.getY() == 0.0f) {
            y += PIN_SIZE / 2.0 + PIN_CAPTION_SPACING;
        } else if (pin.getY() == 1.0f) {
            y -= PIN_SIZE / 2.0 + r.getHeight() + PIN_CAPTION_SPACING;
        } else {
            y -= r.getHeight() / 2.0;
        }
        g.setColor( PIN_CAPTION_COLOR  );
        g.drawString( str, Math.round( x ), Math.round( y ));
    }

    private Point2D getNewPinLocation( Point p ) {
        int px = p.x;
        int py = p.y;
        int dx = Math.min( Math.abs( px ), Math.abs( px - getWidth() + 1 ));
        int dy = Math.min( Math.abs( py ), Math.abs( py - getHeight() + 1 ));
        float x, y;
        if (dx < dy) {
            x = (px < getWidth() / 2) ? 0.0f : 1.0f;
            y = py / (getHeight() - 1.0f);
            if (y < 0.0) {
                y = 0.0f;
            } else if (y > 1.0) {
                y = 1.0f;
            }
        } else {
            y = (py < getHeight() / 2) ? 0.0f : 1.0f;
            x = px / (getWidth() - 1.0f);
            if (x < 0.0) {
                x = 0.0f;
            } else if (x > 1.0) {
                x = 1.0f;
            }
        }
        return new Point2D.Float( x, y );
    }

    private Point relativeToAbsolute( Point2D p ) {
        return new Point(
            (int)(p.getX() * (getWidth() - 1)),
            (int)(p.getY() * (getHeight() - 1)) 
        );
    }
    
    private Shape createPinShape( Point2D relativePosition ) {
        Point p = relativeToAbsolute( relativePosition );
        int r = PIN_SIZE / 2;
        return new Rectangle( p.x - r, p.y - r, r * 2, r * 2 );
    }

    private Shape createAnchorShape( Point2D relativePosition ) {
        Point p = relativeToAbsolute( relativePosition );
        int r = ANCHOR_SIZE / 2;
        return new Rectangle( p.x - r, p.y - r, r * 2 + 1, r * 2 + 1 );
    }

    @Override
    public Pin getPinAt( int x, int y ) {
        float x0 = x / (getWidth() - 1f);
        float y0 = y / (getHeight() - 1f);
        float rx = 0.5f * PIN_SIZE / (getWidth() - 1);
        float ry = 0.5f * PIN_SIZE / (getHeight() - 1);
        for (Pin q : pins) {
            if (Math.abs( q.getX() - x0 ) < rx && Math.abs( q.getY() - y0 ) < ry) {
                return q;
            }
        }
        return null;
    }

    private Anchor getAnchorAt( int x, int y ) {
        float x0 = x / (getWidth() - 1f);
        float y0 = y / (getHeight() - 1f);
        float rx = 0.5f * ANCHOR_SIZE / (getWidth() - 1);
        float ry = 0.5f * ANCHOR_SIZE / (getHeight() - 1);
        for (Anchor a : Anchor.values()) {
            if (Math.abs( a.getX() - x0 ) < rx && Math.abs( a.getY() - y0 ) < ry) {
                return a;
            }
        }
        return null;
    }

    protected Rectangle getResizeOutline( Point p, Anchor anchor ) {
        int x = 0;
        int y = 0;
        int w = getWidth();
        int h = getHeight();
        if (anchor.getX() == 0.0f) {
            int dx = Math.min( snap( p.x ) - x, w - MIN_SIZE );
            x += dx;
            w -= dx;
        } else if (anchor.getX() == 1.0f) {
            w += Math.max( snap( p.x ) - x - w, MIN_SIZE - w );
        }
        if (anchor.getY() == 0.0f) {
            int dy = Math.min( snap( p.y ) - y, h - MIN_SIZE );
            y += dy;
            h -= dy;
        } else if (anchor.getY() == 1.0f) {
            h += Math.max( snap( p.y ) - y - h, MIN_SIZE - h );
        }
        return new Rectangle( x, y, w - 1, h - 1 );
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        boolean stateChanged = false;
        Pin pin = getPinAt( e.getX(), e.getY());
        if (pin != null) {
            e.consume();
        }
        if (currentPin != pin) {
            currentPin = pin;
            stateChanged = true;
            if (currentPin != null) {
                setCursor( HAND_CURSOR );
            }
        }
        Anchor anchor = null;
        if (currentPin == null) {
            anchor = getAnchorAt( e.getX(), e.getY());
            if (anchor != null) {
                e.consume();
            }
        }
        if (currentAnchor != anchor) {
            currentAnchor = anchor;
            stateChanged = true;
            if (anchor != null) {
                setCursor( anchor.getCursor());
            }
        }
        if (stateChanged && currentPin == null && currentAnchor == null) {
            setCursor( DEFAULT_CURSOR );
        }
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
        if (currentPin != null) {
            e.consume();
            paintOutline( createPinShape( getNewPinLocation( e.getPoint())));
        } else if (currentAnchor != null) {
            e.consume();
            paintOutline( getResizeOutline( e.getPoint(), currentAnchor ));
        }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        if (currentPin != null) {
            e.consume();
            Pin pin = currentPin;
            Point2D newLocation = getNewPinLocation( e.getPoint());
            currentPin = null;
            setCursor( DEFAULT_CURSOR );
            doAction( new MovePinAction( this, pin, newLocation ));
        } else if (currentAnchor != null) {
            e.consume();
            Rectangle r = getResizeOutline( e.getPoint(), currentAnchor );
            currentAnchor = null;
            setCursor( DEFAULT_CURSOR );
            doAction( 
                new ResizeComponentAction( this, 
                    r.x, 
                    r.y,
                    r.width - getWidth() + 1,
                    r.height - getHeight() + 1
                )
            );
        } 
    }

    @Override
    public void mousePressed( MouseEvent e ) {}

    @Override
    public void mouseClicked( MouseEvent e ) {}

    @Override
    public void mouseEntered( MouseEvent e ) {}

    @Override
    public void mouseExited( MouseEvent e ) {
        if (currentPin != null || currentAnchor != null) {
            currentPin = null;
            currentAnchor = null;
            setCursor( DEFAULT_CURSOR );
        }
    }

    @Override
    public Shape getMovingShape( Point dp ) {
        return new Rectangle( 
                getX() + dp.x, 
                getY() + dp.y, 
                getWidth() - 1,
                getHeight() - 1 
        );
    }
    
    @Override
    public void repaintComponent() {
        repaintComponent( 
                -HIT_TOLERANCE, 
                -HIT_TOLERANCE,
                getWidth() + 2 * HIT_TOLERANCE,
                getHeight() + 2 * HIT_TOLERANCE
        );
    }

    @Override
    public void reload() {}
    
    public void repaintComponent( int x, int y, int w, int h ) { // Relatively to the container
        BuilderContainer parent = getParent();
        if (parent != null) {
            parent.repaintComponent( x + getX(), y + getY(), w, h );
        }
    }
    
    public void doAction( CanvasAction action ) {
        BuilderContainer parent = getParent();
        if (parent != null) {
            parent.doAction( action );
        }
    }
    
    public void setCursor( Cursor val ) {
        BuilderContainer parent = getParent();
        if (parent != null) {
            parent.setCursor( val );
        }
    }
    
    public GridAttributes getGridAttributes() {
        BuilderContainer parent = getParent();
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
    
}
