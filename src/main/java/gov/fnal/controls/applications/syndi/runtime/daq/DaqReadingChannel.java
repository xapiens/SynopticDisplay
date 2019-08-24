// (c) 2001-2010 Fermi Research Allaince
// $Id: DaqReadingChannel.java,v 1.2 2010/09/15 15:29:45 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

import gov.fnal.controls.tools.timed.TimedNumber;

/**
 * Abstract readable data acquisition channel.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/15 15:29:45 $
 */
public interface DaqReadingChannel extends DaqChannel {
    
    void newData( TimedNumber data );
    
}
