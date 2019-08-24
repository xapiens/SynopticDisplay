// (c) 2001-2010 Fermi Research Alliance
// $Id: SawtoothWaveGenerator.java,v 1.3 2010/09/15 16:36:31 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.simulation;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedDouble;

/**
 * Sawtooth signal generator.
 * 
 * @author  Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:31 $
 */
@DisplayElement(
    name            = "Sawtooth",
    description     = "Sawtooth signal generator",
    group           = "Simulation",
    icon            = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEX///8AAABVwtN+AAAAAXRSTlMA" +
                      "QObYZgAAACtJREFUeNo1wsEJACAMALHrIdpnR3AUR3VURTAErpR6p6yIYaDYMLFwd74DHokBa78uKikA" +
                      "AAAASUVORK5CYII=",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.SimulationComponent",
    helpUrl         = "/SawtoothSimulatedSource",

    properties  = {
        @Property( caption="Width",         name="width",     value="60",   type=Integer.class ),
        @Property( caption="Height",        name="height",    value="20",   type=Integer.class ),
        @Property( caption="Offset",        name="offset",    value="0.0",  type=Double.class  ),
        @Property( caption="Amplitude",     name="amplitude", value="1.0",  type=Double.class  ),
        @Property( caption="Period, s.",    name="period",    value="10.0", type=Double.class  ),
        @Property( caption="Phase, \u00b0", name="phase",     value="0.0",  type=Double.class  ),
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
        
public class SawtoothWaveGenerator extends AbstractGenerator {

    private double _phase;
    
    public SawtoothWaveGenerator() {
        super( "Sawtooth" );
    }

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        _phase = period * phase / 360.0;
    }
    
    @Override
    public void run() {
        long t = System.currentTimeMillis();
        if (t0 == 0) {
            t0 = t;
        }
        double val = offset + amplitude *
                2 * Math.IEEEremainder( 1e-3 * (t - t0) + _phase, period ) / period;
        deliver( new TimedDouble( val, t, null, 0 ));
    }

}
