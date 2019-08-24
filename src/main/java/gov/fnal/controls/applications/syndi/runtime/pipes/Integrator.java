// (c) 2001-2010 Fermi Research Alliance
// $Id: Integrator.java,v 1.3 2010/09/15 16:36:29 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedDouble;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Calculates average value over last several points.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:29 $
 */
@DisplayElement(

    name            = "Integrator",
    description     = "Computes an average value over last several points",
    group           = "Converters",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.PipeComponent",
    helpUrl         = "/Integrator",
        
    properties  = {
        @Property( caption="Width",            name="width",       value="60", type=Integer.class ),
        @Property( caption="Height",           name="height",      value="20", type=Integer.class ),
        @Property( caption="Number of Points", name="pointNumber", value="10", type=Integer.class ),
        @Property( caption="Data Tag",         name="tag",         value="",   required=false     )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 1,
    maxOutputs = 64,
        
    inputs = { 
        @Pin( number = 1, x = 0, y = 0.5 )
    }, 
    
    outputs = {
        @Pin( number = 2, x = 1, y = 0.5 )
    }

)
        
public class Integrator extends AbstractPipe {
    
    private int numPoints;
    private double[] buf;
    private int len = 0;

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        numPoints = props.getValue( Integer.class, "pointNumber", 10 );
        if (numPoints <= 0 || numPoints > 1024) {
            numPoints = 1;
        }
        buf = new double[ numPoints ];
        len = 0;
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        double val = data.doubleValue();
        if (!Double.isNaN( val )) {
            double sum = 0, v;
            for (int i = len - 1; i >= 0; i--) {
                v = buf[ i ];
                sum += v;
                if (i + 1 < numPoints) {
                    buf[ i + 1 ] = v;
                }
            }
            if (len < numPoints) {
                len++;
            }
            buf[ 0 ] = val;
            sum += val;
            deliver( new TimedDouble( sum / len, data.getTime(), null, 0 ));
        }
    }

}
