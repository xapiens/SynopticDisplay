// (c) 2001-2010 Fermi Research Allaince
// $Id: SettingStateListener.java,v 1.2 2010/09/15 15:29:45 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:29:45 $
 */
public interface SettingStateListener {

    void settingStateChanged( boolean settingEnabled );

}
