// (c) 2001-2010 Fermi Research Allaince
// $Id: RuntimeComponent.java,v 1.2 2010/09/15 15:25:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.tools.timed.TimedNumber;
import org.w3c.dom.Element;

/**
 * Base interface for a run-time synoptic component.
 * 
 * @author  Timofei Bolshakov, Andrey Petrov
 * @version $Date: 2010/09/15 15:25:15 $
 */
public interface RuntimeComponent {

    void init( Element source ) throws Exception;
    
    void setInput( int index, RuntimeComponent comp, int reverseIndex );

    RuntimeComponent getInput( int index );

    void setOutput( int index, RuntimeComponent comp, int reverseIndex );
    
    RuntimeComponent getOutput( int index );

    void offer( TimedNumber data, int inputIndex );

    String getDataTag( int outIndex );

    boolean doesSetting();
    
}
