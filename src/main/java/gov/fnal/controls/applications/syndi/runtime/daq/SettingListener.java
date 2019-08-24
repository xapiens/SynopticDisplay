// (c) 2001-2010 Fermi Research Allaince
// $Id: SettingListener.java,v 1.2 2010/09/15 15:29:45 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

import gov.fnal.controls.tools.timed.TimedNumber;

/**
 *
 * @author  Andrey Petrov
 * @version $Date: 2010/09/15 15:29:45 $
 */
public interface SettingListener {

    void newData( TimedNumber data );

}
