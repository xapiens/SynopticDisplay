// (c) 2001-2010 Fermi Research Allaince
// $Id: GenericSVGComponent.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.tools.svg.SVGComponent;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public abstract class GenericSVGComponent<T extends SVGComponent> extends AbstractSVGComponent<T> 
        implements MouseListener, MouseMotionListener, Rotatable {

    private Point[] anchors;
    private Anchor currentAnchor;

    protected GenericSVGComponent( T svg ) {
        super( svg );
    }

    @Override
    public Shape getMovingShape( Point dp ) {
        AffineTransform xform = AffineTransform.getTranslateInstance( getX() + dp.x, getY() + dp.y );
        return xform.createTransformedShape( svg.getOutline());
    }
    
    @Override
    protected Point[] getAnchors() {
        return anchors;
    }

    @Override
    protected void recalculateBounds() {
        super.recalculateBounds();
        Rectangle b = getBounds();
        int tx = b.x - getX();
        int ty = b.y - getY();
        int sx = b.width;
        int sy = b.height;
        if (anchors == null) {
            anchors = new Point[ Anchor.values().length ];
        }
        int i = 0;
        for (Anchor a : Anchor.values()) {
            int x = tx + (int)(a.getX() * sx);
            int y = ty + (int)(a.getY() * sy);
            anchors[ i++ ] = new Point( x, y );
        }
    }

    @Override
    public void rotate() {
        doAction( new RotateAction( this, 1 ));
    }

    protected void applyRotation( int quad ) {
        AffineTransform xform1 = AffineTransform.getQuadrantRotateInstance( quad );
        AffineTransform xform0 = getTransform();
        if (xform0 != null) {
            xform1.concatenate( xform0 );
        }
        setTransform( xform1 );
    }
    
    private Anchor getAnchorAt( Point p ) {
        int r = (int)Math.ceil( 0.5 * ANCHOR_SIZE );
        int i = 0;
        for (Point a : anchors) {
            if (Math.abs( p.x - a.x ) < r && Math.abs( p.y - a.y ) < r) {
                return Anchor.values()[ i ];
            }
            ++i;
        }
        return null;
    }

    private AffineTransform getResizeTransform( Point p, Anchor anchor ) {
        Rectangle r = getBounds();
        double min_dw = MIN_SIZE - r.width;
        double min_dh = MIN_SIZE - r.height;
        r.setLocation( r.x - getX(), r.y - getY());
        double dw = 0, dx = 0;
        switch (anchor) {
            case NORTHWEST:
            case WEST:
            case SOUTHWEST:
                dw = Math.max( r.x - p.x, min_dw );
                dx = -dw;
                break;
            case NORTHEAST:
            case EAST:
            case SOUTHEAST:
                dw = Math.max( p.x - (r.x + r.width), min_dw );
                break;
        }
        double dh = 0, dy = 0;
        switch (anchor) {
            case NORTHWEST:
            case NORTH:
            case NORTHEAST:
                dh = Math.max( r.y - p.y, min_dh );
                dy = -dh;
                break;
            case SOUTHWEST:
            case SOUTH:
            case SOUTHEAST:
                dh = Math.max( p.y - (r.y + r.height), min_dh );
                break;
        }
        double sx = 1.0 + dw / r.width;
        double sy = 1.0 + dh / r.height;
        double tx = dx + r.x * (1.0 - sx);
        double ty = dy + r.y * (1.0 - sy);
        AffineTransform xform = AffineTransform.getTranslateInstance( tx, ty );
        xform.scale( sx, sy );
        return xform;
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        Anchor anchor = getAnchorAt( e.getPoint());
        if (anchor != null) {
            e.consume();
        }
        if (currentAnchor != anchor) {
            currentAnchor = anchor;
            if (anchor != null) {
                setCursor( anchor.getCursor());
            } else {
                setCursor( DEFAULT_CURSOR );
            }
        }
    }
    
    @Override
    public void mouseDragged( MouseEvent e ) {
        if (currentAnchor == null) {
            return;
        }
        e.consume();
        AffineTransform xform = getResizeTransform( snap( e.getPoint()), currentAnchor );
        paintOutline( xform.createTransformedShape( svg.getOutline()));
    }
            
    @Override
    public void mouseReleased( MouseEvent e ) {
        
        if (currentAnchor == null) {
            return;
        }
        e.consume();

        Rectangle r0 = getBounds();
        r0.x -= getX();
        r0.y -= getY();

        AffineTransform xform = getResizeTransform( snap( e.getPoint()), currentAnchor );
        Rectangle r1 = xform.createTransformedShape( r0 ).getBounds();
        r1.x += getX();
        r1.y += getY();

        int dx = calculateShiftOnDrag( r0.width, r1.width, r0.x, currentAnchor.getX());
        int dy = calculateShiftOnDrag( r0.height, r1.height, r0.y, currentAnchor.getY());

        currentAnchor = null;

        r1.x -= dx;
        r1.y -= dy;

        int xx = getX() + dx;
        int yy = getY() + dy;

        doAction( new ResizeSVGAction( this, r1, new Point( xx, yy )));
        setCursor( DEFAULT_CURSOR );
        
    }

    private static int calculateShiftOnDrag(
            int size0,
            int size1,
            int location,
            float relAnchor ) {
        float absAnchor = location + size0 * relAnchor;
        float q = 1 - Math.abs( absAnchor / size0 );
        if (relAnchor == 0) {
            q = -q;
        }
        return (int)((size1 - size0) * q);
    }

    public void setSize( int width, int height ) {

        Rectangle r0 = getBounds();
        if (r0.width == width && r0.height == height) {
            return;
        }

        Rectangle r1 = new Rectangle( r0.x, r0.y, width, height );

        int dx = calculateShiftOnResize( r0.width, r1.width, r0.x - getX());
        int dy = calculateShiftOnResize( r0.height, r1.height, r0.y - getY());

        r1.x -= dx;
        r1.y -= dy;

        setBounds( r1 );

    }

    private static int calculateShiftOnResize(
            int size0,
            int size1,
            int location ) {
        float q = -(float)location / size0;
        return (int)((size1 - size0) * q);
    }

    @Override
    public void mousePressed( MouseEvent e ) {}

    @Override
    public void mouseClicked( MouseEvent e ) {}

    @Override
    public void mouseEntered( MouseEvent e ) {}

    @Override
    public void mouseExited( MouseEvent e ) {
        if (currentAnchor != null) {
            currentAnchor = null;
            setCursor( DEFAULT_CURSOR );
        }
    }

    private Shape createRotationAnchorShape() {
        return new Polygon(
            new int[]{ -ANCHOR_SIZE, 0, ANCHOR_SIZE, 0 },
            new int[]{ 0, ANCHOR_SIZE, 0, -ANCHOR_SIZE },
            4
        );
    }

    @Override
    public void paint( Graphics2D g ) {
        super.paint( g );
        if (isSelected()) {
            g.draw( createRotationAnchorShape());
        }
    }

}
