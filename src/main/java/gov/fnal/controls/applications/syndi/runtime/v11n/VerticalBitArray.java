// (c) 2001-2010 Fermi Research Alliance
// $Id: VerticalBitArray.java,v 1.4 2010/09/15 16:36:30 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.v11n;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:30 $
 */
@DisplayElement(

    name            = "Bit Array - Vertical",
    description     = "A vertical set of bits",
    group           = "State Indicators",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.VisualComponent",
    helpUrl         = "/VerticalBitArray",

    properties = {
        @Property( caption="Width",             name="width",        value="30",        type=Integer.class                 ),
        @Property( caption="Height",            name="height",       value="240",       type=Integer.class                 ),
        @Property( caption="Border",            name="border",       value="black",     type=Color.class,   required=false ),
        @Property( caption="Border Width",      name="borderWidth",  value="1.0",       type=Double.class                  ),
        @Property( caption="Number of Bits",    name="numBits",      value="8",         type=Integer.class                 ),
        @Property( caption="Arrange Backwards", name="backwards",    value="false",     type=Boolean.class                 ),
        @Property( caption="\'0\' Color",       name="fillColor0",   value="limegreen", type=Color.class,   required=false ),
        @Property( caption="\'1\' Color",       name="fillColor1",   value="crimson",   type=Color.class,   required=false )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 0,
    maxOutputs = 64,

    inputs = {
        @Pin( number=0, x=0, y=0.5 )
    }

)

public class VerticalBitArray extends AbstractBitArray {

    public VerticalBitArray() {}

    @Override
    public void paint( Graphics g ) {

        Graphics2D g2 = (Graphics2D)g;

        int width = getWidth();
        int height = getHeight();

        float step = (float)height / numBits;

        boolean error = false;
        boolean[] state = null;

        TimedNumber value_ = value;
        if (value_ != null) {
            double v = value_.doubleValue();
            if (Double.isNaN( v )) {
                error = true;
            } else {
                state = getState( value );
            }
        }

        if (state != null) {
            float yy = 0;
            for (int i = 0; i < numBits; i++, yy += step) {
                Color col = state[ i ] ? fillColor1 : fillColor0;
                if (col == null) {
                    continue;
                }
                g2.setColor( col );
                int y = Math.round( yy );
                int h = Math.round( yy + step ) - y;
                g2.fillRect( 0, y, width - 1, h );
            }
        }

        if (borderColor != null && borderStroke != null) {
            g2.setColor( borderColor );
            g2.setStroke( borderStroke );
            g2.drawRect(
                (int)(borderWidth / 2),
                (int)(borderWidth / 2),
                width - (int)borderWidth,
                height - (int)borderWidth
            );
            float yy = step;
            for (int i = 0; i < numBits; i++, yy += step) {
                int y = Math.round( yy );
                g2.drawLine( 0, y, width - 1, y );
            }
        }

        if (error) {
            g2.setColor( ERROR_CROSS_COLOR );
            g2.drawLine( 0, 0, width - 1, height - 1 );
            g2.drawLine( 0, height - 1, width - 1, 0 );
        }

    }

}
