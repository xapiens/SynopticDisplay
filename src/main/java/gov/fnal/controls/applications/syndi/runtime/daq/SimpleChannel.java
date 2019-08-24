// (c) 2001-2010 Fermi Research Allaince
// $Id: SimpleChannel.java,v 1.3 2010/09/23 15:50:10 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.applications.syndi.runtime.AbstractRuntimeComponent;
import gov.fnal.controls.applications.syndi.runtime.DisplayFormatException;

/**
 * Abstract single-value data acquisition channel.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/23 15:50:10 $
 */
public abstract class SimpleChannel extends AbstractRuntimeComponent implements DaqChannel {

    private String dataRequest;

    protected SimpleChannel() {}

    @Override
    public void init( PropertyCollection props ) throws Exception {
        dataRequest = props.getValue( String.class, "devName" );
        if (dataRequest == null) {
            throw new DisplayFormatException( "Missing data request" );
        }
        String s = dataRequest.toLowerCase();
        if (s.endsWith( ",reading" ) || s.endsWith( ",setting" )) {
            dataRequest = dataRequest.substring( 0, dataRequest.length() - 8 );
        }
    }

    @Override
    public String getDataRequest() {
        return dataRequest;
    }

}
