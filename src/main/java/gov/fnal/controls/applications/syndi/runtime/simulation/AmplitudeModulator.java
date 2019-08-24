// (c) 2001-2010 Fermi Research Alliance
// $Id: AmplitudeModulator.java,v 1.3 2010/09/15 16:36:31 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.simulation;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.RuntimeComponent;
import gov.fnal.controls.tools.timed.TimedDoubleArray;

/**
 * Amplitude-modulated sine wave packed in an arrays.
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 16:36:31 $
 */
@DisplayElement(
    name            = "Modulator",
    description     = "Amplitude-modulated sine wave packed in an arrays.",
    group           = "Simulation",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.SimulationComponent",
    helpUrl         = "/AmplitudeModulator",

    properties  = {
        @Property( caption="Width",               name="width",         value="60",    type=Integer.class                 ),
        @Property( caption="Height",              name="height",        value="40",    type=Integer.class                 ),
        @Property( caption="Amplitude",           name="amplitude",     value="1.0",   type=Double.class                  ),
        @Property( caption="Modulation Depth",    name="depth",         value="0.9",   type=Double.class                  ),
        @Property( caption="Signal Period, s.",   name="period",        value="10.0",  type=Double.class                  ),
        @Property( caption="Carrier Period, s.",  name="carrier",       value="0.1",   type=Double.class                  ),
        @Property( caption="Array Size",          name="arrSize",       value="1024",  type=Integer.class                 ),
        @Property( caption="Full Array Time, s.", name="fullTime",      value="1.0",   type=Double.class                  ),
        @Property( caption="Data Tag",            name="tag",           value="",                          required=false )
    },

    minInputs = 0,
    maxInputs = 0,
    minOutputs = 2,
    maxOutputs = 64,

    outputs = {
        @Pin( number=0, x=1, y=0.33, name="X" ),
        @Pin( number=1, x=1, y=0.67 )
    }
)


public class AmplitudeModulator extends AbstractGenerator {

    private static final int DEFAULT_ARRAY_SIZE = 256;

    double depth, carrier, fullTime, _period, _carrier, timeStep;
    int arraySize;
    
    RuntimeComponent scaleComp;
    int scaleCompIndex;

    public AmplitudeModulator() {
        super( "Modulator" );
    }

    @Override
    public void setOutput( int index, RuntimeComponent comp, int reverseIndex ) {
        if (index == 0) {
            scaleComp = comp;
            scaleCompIndex = reverseIndex;
        } else {
            super.setOutput( index, comp, reverseIndex );
        }
    }

    @Override
    public RuntimeComponent getOutput( int index ) {
        if (index == 0) {
            return scaleComp;
        } else {
            return super.getOutput( index );
        }
    }

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        depth = props.getValue( Double.class, "depth", 0.9 );
        carrier = props.getValue( Double.class, "carrier", 0.1 );
        arraySize = props.getValue( Integer.class, "arrSize", DEFAULT_ARRAY_SIZE );
        if (arraySize <= 0) {
            arraySize = DEFAULT_ARRAY_SIZE;
        }
        fullTime = props.getValue( Double.class, "fullTime", 1.0 );
        _period = period / (2.0 * Math.PI);
        _carrier = carrier / (2.0 * Math.PI);
        timeStep = fullTime / arraySize;
    }

    @Override
    public void run() {
        long t1 = System.currentTimeMillis();
        if (t0 == 0) {
            t0 = t1;
            deliverScale();
        }
        double t = 1e-3 * (t1 - t0);
        double[] val = new double[ arraySize ];
        for (int i = 0; i < arraySize; i++) {
            val[ i ] = amplitude * depth * Math.sin( t / _carrier ) *
                (1.0 / depth + Math.cos( t / _period )) / (1.0 + depth);
            t += timeStep;
        }
        deliver( new TimedDoubleArray( val, t1, null, 0 ));
    }

    private void deliverScale() {
        if (scaleComp == null) {
            return;
        }
        double[] val = new double[ arraySize ];
        double t = 0;
        for (int i = 0; i < arraySize; i++) {
            val[ i ] = t;
            t += timeStep;
        }
        scaleComp.offer( new TimedDoubleArray( val ), scaleCompIndex );
    }

}
