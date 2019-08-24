// (c) 2001-2010 Fermi Research Allaince
// $Id: DaqInterface.java,v 1.3 2010/09/23 15:49:01 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

import java.awt.Component;
import java.util.Set;

/**
 * Pluggable interface to the data acquisition system.
 * <p>
 * Use <code>AbstractDaqInterface</code> as a standard implementation.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/23 15:49:01 $
 */
public interface DaqInterface {

    /**
     * Optionally returns a visual component that is used in the GUI to control
     * this data connection.
     * <p>
     * The Viewer application will place this component on the right end of the
     * status bar.
     *
     * @return a component to control the connection or <code>null</code>.
     */
    Component getControlWidget();

    /**
     * Starts the data acquisition process.
     *
     * @param channels a set of data channels (may be empty, but not <code>null</code>).
     * @throws Exception is thrown is the data acquisition process cannot be started.
     */
    void start( Set<DaqChannel> channels ) throws Exception;

    /**
     * Stops the data acquisition process.
     * <p>
     * This operation should never throw exceptions. If DAQ is not running,
     * the method should terminate silently.
     */
    void stop();

    /**
     * Checks whether the data acquisition process is running.
     *
     * @return <code>true</code> is DAQ is running.
     */
    boolean isRunning();

    /**
     * Checks whether setting is enabled in the instance of the data acquisition
     * interface.
     *
     * @return <code>true</code> is setting is enabled.
     */
    boolean isSettingEnabled();

    /**
     * Adds a listener which is notified when DAQ setting is enabled or disabled.
     *
     * @param l the listener to be added, should not be <code>null</code>.
     */
    void addSettingStateListener( SettingStateListener l );

    /**
     * Removes a setting state listener.
     *
     * @param l the listener to be removed, should not be <code>null</code>.
     */
    void removeSettingStateListener( SettingStateListener l );

}
