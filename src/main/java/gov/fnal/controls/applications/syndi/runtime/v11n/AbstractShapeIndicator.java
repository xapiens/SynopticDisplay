// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractShapeIndicator.java,v 1.2 2010/09/15 15:48:22 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.property.Orientation;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import javax.swing.JComponent;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:48:22 $
 */
public abstract class AbstractShapeIndicator extends AbstractStateIndicator {
    
    private static final double DEFAULT_STROKE_WIDTH = 2;
    
    private final Shape origShape0, origShape1;
    private final boolean invertVertical;

    private Color fillColor0, fillColor1, strokeColor0, strokeColor1;
    private Stroke stroke;
    private Shape shape0, shape1;
    private Orientation orient;
    private float strokeWidth;
    private Rectangle bounds;

    protected AbstractShapeIndicator( Shape shape ) {
        this( shape, false );
    }

    protected AbstractShapeIndicator( Shape shape, boolean invertVertical ) {
        this( shape, shape, invertVertical );
    }

    protected AbstractShapeIndicator( Shape shape0, Shape shape1 ) {
        this( shape0, shape1, false );
    }

    protected AbstractShapeIndicator( Shape shape0, Shape shape1, boolean invertVertical ) {
        origShape0 = shape0;
        origShape1 = shape1;
        this.invertVertical = invertVertical;
    }
    
    @Override
    public void init( PropertyCollection props ) throws Exception {
        
        super.init( props );
        
        fillColor0 = props.findValue( Color.class, "fillColor0", "offColor", "background" );
        fillColor1 = props.findValue( Color.class, "fillColor1", "onColor", "alarmBackground" );
        
        strokeColor0 = props.findValue( Color.class, "strokeColor0", "borderColor" );
        strokeColor1 = props.findValue( Color.class, "strokeColor1", "alarmBorder", "borderColor" );
        
        strokeWidth = props.getValue( Double.class, "strokeWidth", DEFAULT_STROKE_WIDTH ).floatValue();
	if (strokeWidth < 0) {
	    strokeWidth = 0;
	}
        stroke = (strokeWidth != 0) ? new BasicStroke( strokeWidth ) : null;
        
        orient = props.getValue( Orientation.class, "orient" );
        if (orient == null) {
            Integer orientId = props.getValue( Integer.class, "orientation" );
            if (orientId != null) {
                switch (orientId.intValue()) {
                    case 0 :
                        orient = invertVertical ? Orientation.SOUTH : Orientation.NORTH;
                        break;
                    case 1 :
                        orient = invertVertical ? Orientation.NORTH : Orientation.SOUTH;
                        break;
                    case 2 :
                        orient = Orientation.EAST;
                        break;
                    case 3 :
                        orient = Orientation.WEST;
                        break;
                }
            }
        }
        if (orient == null) {
            boolean vertical = props.getValue( Boolean.class, "vertical", true );
            boolean flipped = props.getValue( Boolean.class, "flipped", false );
            if (vertical) {
                orient = flipped ? Orientation.WEST : Orientation.EAST;
            } else {
                orient = flipped ? Orientation.SOUTH : Orientation.NORTH;
            }
        }
        
        AffineTransform xform = new AffineTransform();
        xform.translate( 0, 0 );
        xform.scale( getWidth(), getHeight() );
        xform.rotate( orient.getAngle(), 0.5, 0.5 );
	
	bounds = null;

        if (origShape0 != null) {
            shape0 = xform.createTransformedShape( origShape0 );
	    bounds = shape0.getBounds();
        } else {
            shape0 = null;
        }

        if (origShape1 != null) {
            shape1 = xform.createTransformedShape( origShape1 );
	    if (bounds == null) {
		bounds = shape1.getBounds();
	    } else {
		Rectangle.union( bounds, shape1.getBounds(), bounds );
	    }
        } else {
            shape1 = null;
        }

	if (bounds == null) {
	    bounds = new Rectangle();
	}

	bounds.setLocation(
	    (int)(bounds.getX() - strokeWidth),
	    (int)(bounds.getY() - strokeWidth)
	);
	bounds.setSize(
	    (int)(bounds.getWidth() + 2 * strokeWidth),
	    (int)(bounds.getHeight() + 2 * strokeWidth)
	);

    }
    
    @Override
    public void paint( Graphics g ) {

        super.paint( g );
        
        Graphics2D g2 = (Graphics2D)g;
        
        boolean state = false;
        boolean error = false;
        
        TimedNumber value_ = value;
        if (value_ != null) {
            double v = value_.doubleValue();
            if (Double.isNaN( v )) {
                error = true;
            } else {
                state = (v < minValue || v > maxValue);
            }
        }
        
        if (invert) {
            state = !state;
        }
        
        Shape shape = state ? shape1 : shape0;
        Color fillColor = state ? fillColor1 : fillColor0;
        Color strokeColor = state ? strokeColor1 : strokeColor0;

        Container parent = getParent();
        if (parent instanceof JComponent) {
            Rectangle b = ((JComponent)parent).getVisibleRect();
            g2.setClip(
                b.x - getX(),
                b.y - getY(),
                b.width,
                b.height );
        } else {
            g2.setClip( null );
        }

        if (shape != null && fillColor != null) {
            g2.setColor( fillColor );
            g2.fill( shape );
        }
        
        if (shape != null && strokeColor != null && stroke != null) {
            g2.setColor( strokeColor );
            g2.setStroke( stroke );
            g2.draw( shape );
        }
        
        if (error) {
            g2.setColor( ERROR_CROSS_COLOR );
            g2.drawLine( 0, 0, getWidth() - 1, getHeight() - 1 );
            g2.drawLine( 0, getHeight() - 1, getWidth() - 1, 0 );
        }
            
    }

    @Override
    protected Rectangle getEffectiveBounds() {
	return bounds;
    }
    
}
