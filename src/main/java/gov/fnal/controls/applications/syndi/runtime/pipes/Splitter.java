// (c) 2001-2010 Fermi Research Alliance
// $Id: Splitter.java,v 1.3 2010/09/15 16:36:29 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Copies input values into one or several output channels.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 16:36:29 $
 */
@DisplayElement(

    name            = "Splitter",
    description     = "Copies input values into one or several output channels.",
    group           = "Converters",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.PipeComponent",
    helpUrl         = "/Splitter",

    properties = {
        @Property( caption="Width",    name="width",  value="60", type=Integer.class ),
        @Property( caption="Height",   name="height", value="30", type=Integer.class ),
        @Property( caption="Data Tag", name="tag",    value="",   required=false     )
    },

    minInputs = 1,
    maxInputs = 1,
    minOutputs = 1,
    maxOutputs = 64,

    inputs = { 
        @Pin( number=1, x=0, y=0.5 ) 
    },
    
    outputs  = { 
        @Pin( number=2, x=1, y=0.33 ), 
        @Pin( number=3, x=1, y=0.66 ) 
    }

)

public class Splitter extends AbstractPipe {
    
    public Splitter() {}

    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        deliver( data );
    }

}
