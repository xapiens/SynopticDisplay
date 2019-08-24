// (c) 2001-2010 Fermi Research Alliance
// $Id: ConstantArray.java,v 1.4 2010/09/23 15:50:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.simulation;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.AbstractRuntimeComponent;
import gov.fnal.controls.tools.timed.TimedDoubleArray;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Constant array with values evenly distributed within the range.
 * 
 * @author Andrey Petrov
 * @version $Date: 2010/09/23 15:50:09 $
 */
@DisplayElement(

    name            = "Constant Array",
    description     = "Constant array with values evenly distributed within the range.",
    group           = "Simulation",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.SimulationComponent",
    helpUrl         = "/ConstantArray",

    properties  = {
        @Property( caption="Width",         name="width",     value="60",   type=Integer.class ),
        @Property( caption="Height",        name="height",    value="20",   type=Integer.class ),
        @Property( caption="First Value",   name="val0",      value="-1",   type=Double.class  ),
        @Property( caption="Last Value",    name="val1",      value="1",    type=Double.class  ),
        @Property( caption="Array Size",    name="size",      value="1024", type=Integer.class ),
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

public class ConstantArray extends AbstractRuntimeComponent implements Runnable {

    private double[] data;
    private String dataTag;
    private boolean submitted;

    public ConstantArray() {}

    @Override
    protected void init( PropertyCollection props ) throws Exception {

        Double val0 = props.getValue( Double.class, "val0" );
        Double val1 = props.getValue( Double.class, "val1" );
        int size = props.getValue( Integer.class, "size", 0 );

        if (val0 != null && val1 != null && size > 0) {
            double d0 = val0.doubleValue();
            double d1 = val1.doubleValue();
            if (size == 1) {
                data = new double[]{ d0 };
            } else {
                double q = (d1 - d0) / (size - 1.0);
                data = new double[ size ];
                for (int i = 0; i < size; ++i) {
                    data[ i ] = val0 + q * i;
                }
            }
        }

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
        if (data != null) {
            deliver( new TimedDoubleArray( data ));
        }
        submitted = true;
    }

}
