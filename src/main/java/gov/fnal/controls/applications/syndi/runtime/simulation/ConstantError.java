// (c) 2001-2010 Fermi Research Alliance
// $Id: ConstantError.java,v 1.4 2010/09/23 15:50:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.simulation;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.AbstractRuntimeComponent;
import gov.fnal.controls.tools.timed.TimedError;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Constant error.
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/23 15:50:09 $
 */
@DisplayElement(

    name            = "Constant Error",
    description     = "Constant error.",
    group           = "Simulation",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.SimulationComponent",
    helpUrl         = "/ConstantError",

    properties  = {
        @Property( caption="Width",         name="width",     value="60",   type=Integer.class ),
        @Property( caption="Height",        name="height",    value="20",   type=Integer.class ),
        @Property( caption="Facility Code", name="facCode",   value="0",    type=Integer.class ),
        @Property( caption="Error Number",  name="errNumber", value="0",    type=Integer.class ),
        @Property( caption="Message",       name="message",   value="",     required=false     ),
        @Property( caption="Data Tag",      name="tag",       value="",     required=false     )
    },

    minInputs = 0,
    maxInputs = 0,
    minOutputs = 1,
    maxOutputs = 64,

    outputs = {
        @Pin( number=1, x=1, y=0.5 )
    }
)

public class ConstantError extends AbstractRuntimeComponent implements Runnable {

    private static final int DEFAULT_FACILITY_CODE = 0;
    private static final int DEFAULT_ERROR_NUMBER = 0;

    private int facCode, errNumber;
    private String message, dataTag;
    private boolean submitted;

    public ConstantError() {}

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        facCode = props.getValue( Integer.class, "facCode", DEFAULT_FACILITY_CODE );
        errNumber = props.getValue( Integer.class, "errNumber", DEFAULT_ERROR_NUMBER );
        message = props.getValue( String.class, "message" );
        dataTag = props.getValue( String.class, "tag", "" );
    }

    @Override
    public String getDataTag( int outIndex ) {
        return dataTag;
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {}

    @Override
    public void run() {
        if (submitted) {
            return;
        }
        TimedNumber value = new TimedError(
            facCode,
            errNumber,
            message,
            System.currentTimeMillis()
        );
        deliver( value );
        submitted = true;
    }

}
