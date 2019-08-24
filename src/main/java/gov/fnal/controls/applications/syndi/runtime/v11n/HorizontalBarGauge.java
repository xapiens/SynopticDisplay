// (c) 2001-2010 Fermi Research Alliance
// $Id: HorizontalBarGauge.java,v 1.4 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Bar Gauge - Horizontal",
    description     = "Displays the value as a horizontal bar",
    group           = "Gauges",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/HorizontalBarGauge",

    properties = {
        @Property( caption="Width",                 name="width",       value="250",        type=Integer.class                 ),
        @Property( caption="Height",                name="height",      value="20",         type=Integer.class                 ),
        @Property( caption="Background",            name="background",  value="",           type=Color.class,   required=false ),
        @Property( caption="Fill Color",            name="fillColor",   value="navy",       type=Color.class                   ),
        @Property( caption="Border Color",          name="borderColor", value="black",      type=Color.class                   ),
        @Property( caption="Border Width",          name="borderWidth", value="1.0",        type=Double.class                  ),
        @Property( caption="Minimum Value",         name="min",         value="0",          type=Double.class                  ),
        @Property( caption="Maximum Value",         name="max",         value="1",          type=Double.class                  )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,

    inputs = {
        @Pin( number=1, x=0, y=0.5 )
    }

)

public class HorizontalBarGauge extends AbstractBarGauge {

    public HorizontalBarGauge() {}

    @Override
    protected Shape createFilling( double value ) {
        return new Rectangle(
            borderWidth,
            borderWidth,
            getLevel( value ),
            getHeight() - 2 * borderWidth
        );
    }

    private int getLevel( double value ) {
        if (value < minValue) {
            value = minValue;
        } else if (value > maxValue) {
            value = maxValue;
        }
        double w = getWidth() - 2 * borderWidth;
        double c = (value - minValue) / (maxValue - minValue);
        return (int)Math.round( c * w );
    }

    @Override
    protected Rectangle computeRepaint( Number oldValue, Number newValue ) {
        if (oldValue == null || newValue == null) {
            return new Rectangle( getSize());
        }
        double v0 = oldValue.doubleValue();
        double v1 = newValue.doubleValue();
        if (Double.isNaN( v0 ) || Double.isNaN( v1 )) {
            return new Rectangle( getSize());
        }
        int x1 = getLevel( v0 );
        int x2 = getLevel( v1 );
        int h = getHeight() - 2 * borderWidth;
        int w = Math.abs( x1 - x2 ) + 1;
        int x = Math.min( x1, x2 );
        return new Rectangle( x, borderWidth, w, h );
    }

}
