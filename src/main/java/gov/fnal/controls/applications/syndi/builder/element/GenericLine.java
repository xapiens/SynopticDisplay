// (c) 2001-2010 Fermi Research Allaince
// $Id: GenericLine.java,v 1.2 2010/09/15 16:08:26 apetrov Exp $
package gov.fnal.controls.applications.syndi.builder.element;

import gov.fnal.controls.applications.syndi.property.ColorProperty;
import gov.fnal.controls.applications.syndi.property.ComponentProperty;
import gov.fnal.controls.applications.syndi.property.DoubleProperty;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.property.PropertyException;
import gov.fnal.controls.tools.svg.SVGColor;
import gov.fnal.controls.tools.svg.SVGPath;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:08:26 $
 */
public class GenericLine extends AbstractSVGComponent<SVGPath>
        implements MouseListener, MouseMotionListener {
    
    protected static final Cursor RESHAPE_CURSOR = Cursor.getPredefinedCursor( Cursor.HAND_CURSOR );
    
    private int currentAnchorIndex = -1;
    private Point[] anchors;

    public GenericLine( SVGPath svg ) {
        super( svg );
        if (svg.getFillColor() == null) {
            svg.setFillColor( SVGColor.NO_COLOR );
        }
        props.add( new ColorProperty( "fill", "Fill Color", false, getFillColor()));
        props.add( new ColorProperty( "stroke", "Stroke Color", false, getStrokeColor()));
        props.add( new DoubleProperty( "stroke-width", "Stroke Width", true, getStrokeWidth()));
    }
    
    @Override
    public boolean contains( Point p ) {
        Path2D path = svg.getPath();
        if (path == null) {
            return false;
        }
        return path.intersects(
            p.x - getX() - HIT_TOLERANCE,
            p.y - getY() - HIT_TOLERANCE,
            2 * HIT_TOLERANCE,
            2 * HIT_TOLERANCE
        );
    }

    public void setStrokeColor( Color val ) {
        svg.setStrokeColor( val );
    }

    public Color getStrokeColor() {
        return svg.getStrokeColor();
    }

    public void setFillColor( Color val ) {
        svg.setFillColor( val );
    }

    public Color getFillColor() {
        return svg.getFillColor();
    }

    public void setStrokeWidth( double width ) {
        svg.setStrokeWidth( width );
    }
    
    public Double getStrokeWidth() {
        Number width = svg.getStrokeWidth();
        return (width == null) ? null : new Double( width.doubleValue());
    }
    
    @Override
    public void setProperties( Collection<ComponentProperty<?>> val ) throws PropertyException {
        super.setProperties( val );
        setFillColor( props.getValue( Color.class, "fill", SVGColor.NO_COLOR ));
        setStrokeColor( props.getValue( Color.class, "stroke", SVGColor.NO_COLOR ));
        setStrokeWidth( props.getValue( Double.class, "stroke-width", getStrokeWidth()));
        repaintComponent();
    }

    @Override
    public Collection<ComponentProperty<?>> getProperties() {
        PropertyCollection res = (PropertyCollection)props.clone();
        res.setComponent( this );
        return res;
    }

    @Override
    protected Point[] getAnchors() {
        return anchors;
    }

    @Override
    protected void recalculateBounds() {
        super.recalculateBounds();
        Path2D path = svg.getPath();
        if (path == null) {
            anchors = new Point[ 0 ];
            return;
        }
        List<Point> res = new ArrayList<Point>();
        float[] coor = new float[ 6 ];
        for (PathIterator z = path.getPathIterator( null ); !z.isDone(); z.next()) {
            switch (z.currentSegment( coor )) {
                case PathIterator.SEG_MOVETO:
                    res.add( createPoint( coor, 0 ));
                    break;
                case PathIterator.SEG_LINETO:
                    res.add( createPoint( coor, 0 ));
                    break;
                case PathIterator.SEG_QUADTO:
                    res.add( createPoint( coor, 2 ));
                    break;
                case PathIterator.SEG_CUBICTO:
                    res.add( createPoint( coor, 4 ));
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
            }
        }
        anchors = new Point[ res.size()];
        res.toArray( anchors );
    }

    private Point createPoint( float[] coor, int offset ) {
        return new Point(
            Math.round( coor[ offset ]),
            Math.round( coor[ offset + 1 ])
        );
    }

    @Override
    public Shape getMovingShape( Point dp ) {
        Shape outline = svg.getOutline();
        if (outline == null) {
            return null;
        }
        GeneralPath path = new GeneralPath( outline );
        AffineTransform xform = AffineTransform.getTranslateInstance( getX() + dp.x, getY() + dp.y );
        return xform.createTransformedShape( path );
    }

    public Path2D getSegmentMovingShape( Point dp ) {
        Shape outline = svg.getOutline();
        if (outline == null) {
            return null;
        }
        GeneralPath path = new GeneralPath( outline );
        path.transform( AffineTransform.getTranslateInstance( getX(), getY()));
        appendPoint( dp.x, dp.y, path );
        return path;
    }

    public void placeNewPoint( int x, int y ) {
        Path2D path = svg.getPath();
        if (path == null) {
            path = new Path2D.Float();
            svg.setPath( path );
        }
        Path2D path0 = (Path2D)path.clone();
        if (path.getCurrentPoint() == null) {
            setLocation( x, y );
        }
        x -= getX();
        y -= getY();
        if (appendPoint( x, y, path )) {
            firePropertyChange( "shape", path0, path );
            recalculateBounds();
        }
    }

    private boolean appendPoint( float x, float y, Path2D path ) {
        Point2D p0 = path.getCurrentPoint();
        if (p0 == null) {
            path.moveTo( x, y );
            return true;
        } else if (!p0.equals( new Point2D.Float( x, y ))) {
            path.lineTo( x, y );
            return true;
        } else {
            return false;
        }
    }

    private Path2D getResizeOutline( int x, int y, int anchorIndex ) {
        Path2D path0 = getPath();
        Path2D path1 = new GeneralPath();
        if (path0 != null) {
            int i = 0;
            float[] coor = new float[ 6 ];
            for (PathIterator z = path0.getPathIterator( null ); !z.isDone(); z.next()) {
                if (i++ == anchorIndex) {
                    appendPoint( snap( x ), snap( y ), path1 );
                    continue;
                }
                switch (z.currentSegment( coor )) {
                    case PathIterator.SEG_MOVETO:
                        path1.moveTo( coor[ 0 ], coor[ 1 ]);
                        break;
                    case PathIterator.SEG_LINETO:
                        path1.lineTo( coor[ 0 ], coor[ 1 ]);
                        break;
                    case PathIterator.SEG_QUADTO:
                        path1.quadTo( coor[ 0 ], coor[ 1 ], coor[ 2 ], coor[ 3 ]);
                        break;
                    case PathIterator.SEG_CUBICTO:
                        path1.curveTo( coor[ 0 ], coor[ 1 ], coor[ 2 ], coor[ 3 ], coor[ 4 ], coor[ 5 ]);
                        break;
                    case PathIterator.SEG_CLOSE:
                        path1.closePath();
                        break;
                }
            }
        }
        return path1;
    }

    public Path2D getPath() {
        return svg.getPath();
    }

    public void setPath( Path2D path ) {
        Path2D path0 = svg.getPath();
        svg.setPath( path );
        recalculateBounds();
        firePropertyChange( "shape", path0, path );
    }

    private int getAnchorIndexAt( Point p ) {
        int r = (int)Math.ceil( 0.5 * ANCHOR_SIZE );
        int i = 0;
        for (Point a : anchors) {
            if (Math.abs( p.x - a.x ) < r && Math.abs( p.y - a.y ) < r) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        int anchorIndex = getAnchorIndexAt( e.getPoint());
        if (anchorIndex != -1) {
            e.consume();
        }
        if (currentAnchorIndex != anchorIndex) {
            currentAnchorIndex = anchorIndex;
            setCursor( (anchorIndex != -1) ? RESHAPE_CURSOR : DEFAULT_CURSOR );
        }
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
        if (currentAnchorIndex == -1) {
            return;
        }
        e.consume();
        paintOutline( getResizeOutline( e.getX(), e.getY(), currentAnchorIndex ));
    }
            
    @Override
    public void mouseReleased( MouseEvent e ) {
        if (currentAnchorIndex == -1) {
            return;
        }
        e.consume();
        Path2D path0 = getPath();
        Path2D path1 = getResizeOutline( e.getX(), e.getY(), currentAnchorIndex );
        currentAnchorIndex = -1;
        setCursor( DEFAULT_CURSOR );
        doAction( new ReshapeAction( this, path0, path1 ));
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
    }

    @Override
    public void mousePressed( MouseEvent e ) {
    }

    @Override
    public void mouseEntered( MouseEvent e ) {
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        if (currentAnchorIndex != -1) {
            currentAnchorIndex = -1;
            setCursor( DEFAULT_CURSOR );
        }
    }
    
}