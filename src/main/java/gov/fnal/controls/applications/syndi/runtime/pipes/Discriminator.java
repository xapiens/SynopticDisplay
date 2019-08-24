// (c) 2001-2010 Fermi Research Alliance
// $Id: Discriminator.java,v 1.3 2010/09/15 16:36:29 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Compares input with a threshold and copies the input value into an output channel.
 * <p>
 * If the input value is greater then the threshold, the input is copied into the first
 * output, otherwise into the second one.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:29 $
 */
@DisplayElement(
    name            = "Discriminator",
    description     = "Compares input with a threshold and copies the input value into an output channel. " +
                      "If the input value is greater then the threshold, the input is copied into the first output, " +
                      "otherwise into the second one.",
    group           = "Converters",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.PipeComponent",
    helpUrl         = "/Discriminator",

    properties  = {
        @Property( caption="Width",     name="width",     value="60",    type=Integer.class ),
        @Property( caption="Height",    name="height",    value="30",    type=Integer.class ),
        @Property( caption="Threshold", name="threshold", value="0.0",   type=Double.class  ),
        @Property( caption="Invert",    name="invert",    value="false", type=Boolean.class ),
        @Property( caption="Data Tag",  name="tag",       value="",      required=false     )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 2,
    maxOutputs = 2,

    inputs = { 
        @Pin( number=1, x=0, y=0.5 )
    },
    
    outputs = { 
        @Pin( number=2, x=1, y=0.33, name="GR" ),
        @Pin( number=3, x=1, y=0.66, name="LE" )
    }

)
        
public class Discriminator extends AbstractPipe {
    
    private double threshold;
    private boolean invert;

    public Discriminator() {}
    
    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        threshold = props.getValue( Double.class, "threshold", 0.0 );
        invert = props.getValue( Boolean.class, "invert", false );
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        double val = data.doubleValue();
        if (Double.isNaN( val )) {
            deliver( data, 2 );
            deliver( data, 3 );
        } else {
            boolean result = val > threshold;
            if (invert) {
                result = !result;
            }
            deliver( data, result ? 2 : 3 );
        }
    }

}
