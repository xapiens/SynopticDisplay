// (c) 2001-2010 Fermi Research Alliance
// $Id: LineIndicator.java,v 1.3 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.LineOrientation;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Line Indicator",
    description     = "A line state indicator.",
    group           = "State Indicators",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/LineStateIndicator",

    properties = {
        @Property( caption="Width",                         name="width",         value="40",         type=Integer.class                 ),
        @Property( caption="Height",                        name="height",        value="40",         type=Integer.class                 ),
        @Property( caption="Minimum Normal Value",          name="minValue",      value="0.0",        type=Double.class,  required=false ),
        @Property( caption="Maximum Normal Value",          name="maxValue",      value="0.0",        type=Double.class,  required=false ),
        @Property( caption="Invert State",                  name="invert",        value="false",      type=Boolean.class, required=false ),
        @Property( caption="Orientation",                   name="orient",        value="HORIZONTAL", type=LineOrientation.class             ),
        @Property( caption="Stroke Color FALSE/OFF/NORMAL", name="strokeColor0",  value="limegreen",  type=Color.class,   required=false ),
        @Property( caption="Stroke Color TRUE/ON/ALARM",    name="strokeColor1",  value="crimson",    type=Color.class,   required=false ),
        @Property( caption="Stroke Width",                  name="strokeWidth",   value="1.0",        type=Double.class                  )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,

    inputs = {
        @Pin( number=1, x=0, y=0.5 )
    }

)

public class LineIndicator extends AbstractStateIndicator {

    private static final double DEFAULT_STROKE_WIDTH = 1;
    private static final LineOrientation DEFAULT_ORIENTATION = LineOrientation.HORIZONTAL;

    private Color strokeColor0, strokeColor1;
    private Stroke stroke;
    private Shape shape;
    private LineOrientation orient;
    private float strokeWidth;
    private Rectangle bounds;

    public LineIndicator() {}

    @Override
    public void init( PropertyCollection props ) throws Exception {
        super.init( props );

        strokeColor0 = props.findValue( Color.class, "strokeColor0", "borderColor" );
        strokeColor1 = props.findValue( Color.class, "strokeColor1", "alarmBorder", "borderColor" );

        strokeWidth = props.getValue( Double.class, "strokeWidth", DEFAULT_STROKE_WIDTH ).floatValue();
	if (strokeWidth < 0) {
	    strokeWidth = 0;
	}
        stroke = (strokeWidth != 0) ? new BasicStroke( strokeWidth ) : null;

        orient = props.getValue( LineOrientation.class, "orient", DEFAULT_ORIENTATION );

        switch (orient) {
            case HORIZONTAL :
                shape = new Line2D.Double( 0, getHeight() / 2.0, getWidth(), getHeight() / 2.0 );
                break;
            case VERTICAL :
                shape = new Line2D.Double( getWidth() / 2.0, 0, getWidth() / 2.0, getHeight());
                break;
            case FORWARD_SLASH :
                shape = new Line2D.Double( 0, getHeight(), getWidth(), 0 );
                break;
            case BACK_SLASH :
                shape = new Line2D.Double( 0, 0, getWidth(), getHeight());
                break;
            default :
                shape = null;
        }

	if (shape != null) {
	    bounds = shape.getBounds();
	} else {
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

        Color strokeColor = state ? strokeColor1 : strokeColor0;

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
