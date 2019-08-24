// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractGenerator.java,v 1.3 2010/09/23 15:50:09 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.simulation;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.AbstractRuntimeComponent;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Abstract implementation of a test signal generator.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/23 15:50:09 $
 */
public abstract class AbstractGenerator extends AbstractRuntimeComponent implements Runnable {

    private final String name;

    protected double amplitude, offset, period, phase;
    protected long t0;
    protected String dataTag;

    protected AbstractGenerator( String name ) {
        this.name = name;
    }

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        amplitude = props.getValue( Double.class, "amplitude", 1.0 );
        offset = props.getValue( Double.class, "offset", 0.0 );
        period = props.getValue( Double.class, "period", 10.0 );
        phase = props.getValue( Double.class, "phase", 0.0 );
        dataTag = props.getValue( String.class, "tag", name );
    }

    @Override
    public String getDataTag( int outIndex ) {
        return (dataTag == null) ? name : dataTag;
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {}

}
