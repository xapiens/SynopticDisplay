// (c) 2001-2010 Fermi Research Allaince
// $Id: AbstractRuntimeComponent.java,v 1.1 2010/09/23 15:50:10 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.property.PropertyCollection;
import gov.fnal.controls.tools.timed.TimedNumber;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/23 15:50:10 $
 */
public abstract class AbstractRuntimeComponent implements RuntimeComponent {

    private final Map<Integer,Handle> outputs = new HashMap<Integer,Handle>();

    private RuntimeComponent input;
    private int reverseInputIndex;
    private String dataTag;
    private Boolean doesSetting;

    protected AbstractRuntimeComponent() {}

    @Override
    public final void init( Element source ) throws Exception {
        init( PropertyCollection.create( source ));
    }
    
    protected abstract void init( PropertyCollection props ) throws Exception;

    @Override
    public void setInput( int index, RuntimeComponent comp, int reverseIndex ) {
        // This implementation supports only one input
        input = comp;
        reverseInputIndex = reverseIndex;
    }

    @Override
    public RuntimeComponent getInput( int index ) {
        return input;
    }

    @Override
    public void setOutput( int index, RuntimeComponent comp, int reverseIndex ) {
        outputs.put( index, new Handle( comp, reverseIndex ));
    }

    @Override
    public RuntimeComponent getOutput( int index ) {
        Handle h = outputs.get( index );
        return (h == null) ? null : h.comp;
    }

    @Override
    public String getDataTag( int outIndex ) {
        // outIndex isn't used in this implementation
        if (dataTag == null && input != null) {
            dataTag = input.getDataTag( reverseInputIndex );
        }
        return dataTag;
    }

    @Override
    public boolean doesSetting() {
        if (doesSetting == null) {
            doesSetting = Boolean.FALSE;
            for (Handle h : outputs.values()) {
                if (h.comp.doesSetting()) {
                    doesSetting = Boolean.TRUE;
                    break;
                }
            }
        }
        return doesSetting.booleanValue();
    }

    public void deliver( TimedNumber data ) {
        for (Handle h : outputs.values()) {
            h.offer( data );
        }
    }

    public void deliver( TimedNumber data, int index ) {
        Handle h = outputs.get( index );
        if (h != null) {
            h.offer( data );
        }
    }

    private static class Handle {

        final RuntimeComponent comp;
        final int reverseIndex;

        Handle( RuntimeComponent comp, int reverseIndex ) {
            this.comp = comp;
            this.reverseIndex = reverseIndex;
        }

        void offer( TimedNumber data ) {
            comp.offer( data, reverseIndex );
        }

    }


}
