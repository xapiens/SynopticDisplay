// (c) 2001-2010 Fermi Research Alliance
// $Id: SineWaveGenerator.java,v 1.3 2010/09/15 16:36:31 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.simulation;

import gov.fnal.controls.applications.syndi.markup.DisplayElement;
import gov.fnal.controls.applications.syndi.markup.Pin;
import gov.fnal.controls.applications.syndi.markup.Property;
import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedDouble;

/**
 * Sine wave generator.
 * 
 * @author  Timofei Bolshakov
 * @version $Date: 2010/09/15 16:36:31 $
 */
@DisplayElement(
    name            = "Sine",
    description     = "Sine wave generator",
    group           = "Simulation",
    icon            = "iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAABlBMVEX///8AAABVwtN+AAAAAXRSTlMA" +
                      "QObYZgAAACpJREFUeNpjEGBgkmBgYmBgkgGTdmCSgYORiQ3E+M8BJicASWaGDwwMDAA9HgQ1kIyL0QAA" +
                      "AABJRU5ErkJggg==",
    designTimeView  = "gov.fnal.controls.applications.syndi.builder.element.variant.SimulationComponent",
    helpUrl         = "/SineSimulatedSource",

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
        
public class SineWaveGenerator extends AbstractGenerator {

    private double _period, _phase;

    public SineWaveGenerator() {
        super( "Sine" );
    }

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        super.init( props );
        _period = period / (2.0 * Math.PI);
        _phase = phase * Math.PI / 180.0;
    }

    @Override
    public void run() {
        long t = System.currentTimeMillis();
        if (t0 == 0) {
            t0 = t;
        }
        double val = offset + amplitude *
                Math.sin( 1e-3 * (t - t0) / _period + _phase );
        deliver( new TimedDouble( val, t, null, 0 ));
    }
    
}
