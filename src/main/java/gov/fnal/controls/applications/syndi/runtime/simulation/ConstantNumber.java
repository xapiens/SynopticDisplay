// (c) 2001-2010 Fermi Research Alliance
// $Id: ConstantNumber.java,v 1.4 2010/09/23 15:50:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.simulation;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.AbstractRuntimeComponent;
import gov.fnal.controls.tools.timed.TimedDouble;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Constant number.
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/23 15:50:09 $
 */
@DisplayElement(

    name            = "Constant Number",
    description     = "Constant number.",
    group           = "Simulation",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.SimulationComponent",
    helpUrl         = "/ConstantNumber",

    properties  = {
        @Property( caption="Width",         name="width",     value="60",   type=Integer.class ),
        @Property( caption="Height",        name="height",    value="20",   type=Integer.class ),
        @Property( caption="Value",         name="value",     value="0",    type=Double.class  ),
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

public class ConstantNumber extends AbstractRuntimeComponent implements Runnable {

    private Double value;
    private String dataTag;
    private boolean submitted;

    public ConstantNumber() {}

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        value = props.getValue( Double.class, "value" );
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
        if (value != null) {
            deliver( new TimedDouble( value ));
        }
        submitted = true;
    }

}
