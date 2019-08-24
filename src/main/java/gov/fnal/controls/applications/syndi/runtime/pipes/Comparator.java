// (c) 2001-2010 Fermi Research Alliance
// $Id: Comparator.java,v 1.3 2010/09/15 16:36:29 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedBoolean;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Compares input with a threshold and returns boolean result.
 * <p>
 * The result is true if the input value is equal to or greater then the threshold.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:29 $
 */
@DisplayElement(
    
    name            = "Comparator",
    description     = "Compares the input with a threshold and returns boolean result. " +
                      "The result is true if the input value is greater then the threshold.",
    group           = "Converters",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.PipeComponent",
    helpUrl         = "/Comparator",
    
    properties = {
        @Property( caption="Width",        name="width",       value="60",         type=Integer.class ),
        @Property( caption="Height",       name="height",      value="20",         type=Integer.class ),
        @Property( caption="Threshold",    name="threshold",   value="0.0",        type=Double.class  ),
        @Property( caption="Invert",       name="invert",      value="false",      type=Boolean.class ),
        @Property( caption="Data Tag",     name="tag",         value="",           required=false     )
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

public class Comparator extends AbstractPipe {
    
    private double threshold;
    private boolean invert;
    private Boolean lastResult;

    public Comparator() {}

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
            deliver( data );
        } else {
            boolean result = val > threshold;
            if (invert) {
                result = !result;
            }
            if (lastResult == null || lastResult.booleanValue() != result) {
                lastResult = new Boolean( result );
                deliver( new TimedBoolean( result, data.getTime(), null, 0 ));
            }
        }
    }

}
