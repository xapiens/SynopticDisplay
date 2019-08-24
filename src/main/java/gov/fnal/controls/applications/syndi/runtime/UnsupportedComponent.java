// (c) 2001-2010 Fermi Research Allaince
// $Id: UnsupportedComponent.java,v 1.3 2010/09/15 15:25:14 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.tools.timed.TimedNumber;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:14 $
 */
@Deprecated
public class UnsupportedComponent implements RuntimeComponent {

    private final Map<Integer,RuntimeComponent> inputs = new HashMap<Integer,RuntimeComponent>();
    private final Map<Integer,RuntimeComponent> outputs = new HashMap<Integer,RuntimeComponent>();
    
    public UnsupportedComponent() {}

    @Override
    public void init( Element source ) {}

    @Override
    public void setInput( int index, RuntimeComponent comp, int reverseIndex ) {
        inputs.put( index, comp );
    }

    @Override
    public RuntimeComponent getInput( int index ) {
        return inputs.get( index );
    }

    @Override
    public void setOutput( int index, RuntimeComponent comp, int reverseIndex ) {
        outputs.put( index, comp );
    }

    @Override
    public RuntimeComponent getOutput( int index ) {
        return outputs.get( index );
    }

    @Override
    public void offer( TimedNumber data, int inputIndex ) {}

    @Override
    public String getDataTag( int outIndex ) {
        return null;
    }

    @Override
    public boolean doesSetting() {
        return false;
    }

}
