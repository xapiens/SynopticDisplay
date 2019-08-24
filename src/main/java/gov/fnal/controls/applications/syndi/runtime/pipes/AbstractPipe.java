// (c) 2001-2010 Fermi Research Alliance
// $Id: AbstractPipe.java,v 1.3 2010/09/23 15:50:10 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.pipes;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.AbstractRuntimeComponent;
import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Abstract implementation of a data transformer with one input and multiple outputs.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/23 15:50:10 $
 */
public abstract class AbstractPipe extends AbstractRuntimeComponent {

    protected String dataTag;

    protected AbstractPipe() {}

    @Override
    protected void init( PropertyCollection props ) throws Exception {
        dataTag = props.getValue( String.class, "tag" );
    }

    @Override
    public abstract void offer( TimedNumber data, int inputIndex );

    @Override
    public String getDataTag( int outIndex ) {
        return (dataTag == null) ? super.getDataTag( outIndex ) : dataTag;
    }

}
