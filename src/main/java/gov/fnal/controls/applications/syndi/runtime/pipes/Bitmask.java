// (c) 2001-2010 Fermi Research Alliance
// $Id: Bitmask.java,v 1.3 2010/09/15 16:36:29 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.DisplayFormatException;
import gov.fnal.controls.tools.timed.TimedBoolean;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Bitwise AND of the integer input and a bitmask. Returns true is the result is not 0.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:29 $
 */
@DisplayElement(

    name            = "Bitmask",
    description     = "Bitwise AND of the integer input and a bitmask. Returns true is the result is not 0.",
    group           = "Converters",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.PipeComponent",
    helpUrl         = "/Bitmask",

    properties  = {
        @Property( caption="Width",    name="width",  value="60",    type=Integer.class ),
        @Property( caption="Height",   name="height", value="20",    type=Integer.class ),
        @Property( caption="Mask",     name="mask",   value="0x1"                       ),
        @Property( caption="Invert",   name="invert", value="false", type=Boolean.class ),
        @Property( caption="Data Tag", name="tag",    value="",      required=false     )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 1,
    maxOutputs = 64,

    inputs = { 
        @Pin( number=1, x=0, y=0.5 )
    },
    
    outputs = { 
        @Pin( number=2, x=1, y=0.5 )
    }

)
        
public class Bitmask extends AbstractPipe {
    
    private int mask;
    private boolean invert;
    private Boolean lastResult;
    
    public Bitmask() {}
    
    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        String str = props.getValue( String.class, "mask", "1" );
        try {
            if (str.startsWith( "0x" )) {
                mask = Integer.parseInt( str.substring( 2 ), 16 );
            } else if (str.startsWith( "0" )) {
                mask = Integer.parseInt( str.substring( 1 ), 8 );
            } else {
                mask = Integer.parseInt( str );
            }
        } catch (Exception ex) {
            throw new DisplayFormatException( "Illegal bitmask: " + str );
        }
        invert = props.getValue( Boolean.class, "invert", false );
    }
    
    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        if (Double.isNaN( data.doubleValue())) {
            deliver( data );
        } else {
            boolean result = (data.intValue() & mask) != 0;
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
