// (c) 2001-2010 Fermi Research Alliance
// $Id: Multiplexer.java,v 1.3 2010/09/15 16:36:29 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedError;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.util.HashMap;
import java.util.Map;

/**
 * Selects one of many inputs and forwards it to a single output.
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:29 $
 */
@DisplayElement(

    name            = "Multiplexer",
    description     = "Selects one of many inputs and forwards it to a single output.",
    group           = "Converters",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.PipeComponent",
    helpUrl         = "/Multiplexer",

    properties  = {
        @Property( caption="Width",         name="width",    value="60", type=Integer.class ),
        @Property( caption="Height",        name="height",   value="30", type=Integer.class ),
        @Property( caption="Default Input", name="defInput", value="1",  type=Integer.class, required=false ),
        @Property( caption="Data Tag",      name="tag",      value="",   required=false     )
    },

    minInputs = 2,
    maxInputs = 64,
    minOutputs = 1,
    maxOutputs = 1,

    inputs = {
        @Pin( number=0, x=0, y=0.33, name="Select" ),
        @Pin( number=1, x=0, y=0.66 )
    },

    outputs = {
        @Pin( number=-1, x=1, y=0.5 )
    }

)

public class Multiplexer extends AbstractPipe {
    
    private static final int SELECT_INPUT_INDEX = 0;

    private static final String DEFAULT_DATA_TAG = "MUX";

    private final Map<Integer,TimedNumber> cache = new HashMap<Integer,TimedNumber>();

    private Integer selectedIndex;

    public Multiplexer() {}

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        selectedIndex = props.getValue( Integer.class, "defInput" );
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {
        if (inputIndex == SELECT_INPUT_INDEX) {
            selectInput( data.intValue());
            return;
        }
        cache.put( inputIndex, data );
        if (selectedIndex != null && inputIndex == selectedIndex.intValue()) {
            deliver( data );
        }
    }

    private void selectInput( int selectedIndex ) {
        if (this.selectedIndex != null && selectedIndex == this.selectedIndex.intValue()) {
            return;
        }
        this.selectedIndex = selectedIndex;
        TimedNumber data = cache.get( this.selectedIndex );
        if (data != null) {
            deliver( data );
        } else {
            deliver( new TimedError( 0, 0, "No Input", System.currentTimeMillis()));
        }
    }

    @Override
    public String getDataTag( int outIndex ) {
        return (dataTag == null) ? DEFAULT_DATA_TAG : dataTag;
    }

}
