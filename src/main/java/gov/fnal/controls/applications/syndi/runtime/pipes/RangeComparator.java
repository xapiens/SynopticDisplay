// (c) 2001-2010 Fermi Research Alliance
// $Id: RangeComparator.java,v 1.3 2010/09/15 16:36:29 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedBoolean;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Compares the signal input with the minimum and the maximum inputs.
 * <p>
 * The result is false if the signal is within the range, true otherwise.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/15 16:36:29 $
 */
@DisplayElement(
    
    name            = "Range Comparator",
    description     = "Compares the signal with the minimum and the maximum values provided via inputs. " +
                      "Returns false if the signal is within the range, true otherwise.",
    group           = "Converters",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.PipeComponent",
    helpUrl         = "/RangeComparator",
    
    properties = {
        @Property( caption="Width",        name="width",       value="60",         type=Integer.class ),
        @Property( caption="Height",       name="height",      value="40",         type=Integer.class ),
        @Property( caption="Invert",       name="invert",      value="false",      type=Boolean.class ),
        @Property( caption="Data Tag",     name="tag",         value="",           required=false     )
    },

    minInputs = 3,
    maxInputs = 3,
    minOutputs = 1,
    maxOutputs = 64,
    
    inputs = { 
        @Pin( number = 1, x = 0, y = 0.25, name = "Signal" ),
        @Pin( number = 2, x = 0, y = 0.5,  name = "Min" ),
        @Pin( number = 3, x = 0, y = 0.75, name = "Max" )
    }, 
    
    outputs = { 
        @Pin( number = 4, x = 1, y = 0.5 )
    }

)

public class RangeComparator extends AbstractPipe {
    
    private boolean invert;
    private TimedNumber data;
    private Boolean lastResult;
    private double min = Double.NaN, max = Double.NaN;

    public RangeComparator() {}

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        invert = props.getValue( Boolean.class, "invert", false );
    }
    
    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        switch (inputIndex) {
            case 1 : // signal
                this.data = data;
                process();
                break;
            case 2 : // min
                min = data.doubleValue();
                process();
                break;
            case 3 : // max
                max = data.doubleValue();
                process();
                break;
        }
    }

    private void process() {
        if (Double.isNaN( min ) || Double.isNaN( max ) || data == null) {
            return;
        }
        double val = data.doubleValue();
        if (Double.isNaN( val )) {
            deliver( data );
        } else {
            boolean result = val < min || val > max;
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
