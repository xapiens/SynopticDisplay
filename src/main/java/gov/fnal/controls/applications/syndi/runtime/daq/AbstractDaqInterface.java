// (c) 2001-2010 Fermi Research Allaince
// $Id: AbstractDaqInterface.java,v 1.4 2010/09/23 15:49:01 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime.daq;

import java.awt.Component;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standard implementation of the data acquisition interface.
 * 
 * @author  Andrey Petrov
 * @version $Date: 2010/09/23 15:49:01 $
 */
public abstract class AbstractDaqInterface implements DaqInterface {

    private static final Logger log = Logger.getLogger( AbstractDaqInterface.class.getName());

    private final Set<SettingStateListener> listeners = new CopyOnWriteArraySet<SettingStateListener>();
    private boolean running = false;
    
    protected AbstractDaqInterface() {}

    @Override
    public final synchronized void start( Set<DaqChannel> channels ) throws Exception {
        if (channels == null) {
            throw new NullPointerException();
        }
        if (running) {
            throw new IllegalStateException( "DAQ already running" );
        }
        if (log.isLoggable( Level.FINE )) {
            StringBuilder buf = new StringBuilder();
            for (DaqChannel c : channels) {
                buf.append( "\n    " );
                buf.append( c );
            }
            log.fine( "Starting data acquisition:" + buf );
        }
        startInternal( channels );
        running = true;
    }

    @Override
    public final synchronized void stop() {
        if (!running) {
            return;
        }
        try {
            stopInternal();
        } catch (Throwable ex) {
            log.throwing( getClass().getName(), "stop", ex );
        } finally {
            running = false;
        }
    }

    @Override
    public final boolean isRunning() {
        return running;
    }

    @Override
    public abstract boolean isSettingEnabled();

    @Override
    public abstract Component getControlWidget();

    /**
     * An internal routine for starting the data acquisition process.
     * <p>
     * To be implemented in the subclass. This class guarantees orderly calls to this method.
     *
     * @param channels a set of data channels (may be empty, but not <code>null</code>).
     * @throws Exception is thrown is the data acquisition process cannot be started.
     */
    protected abstract void startInternal( Set<DaqChannel> channels ) throws Exception;

    /**
     * An internal routine for stopping the data acquisition process.
     * <p>
     * To be implemented in the subclass. This class guarantees orderly calls to this method.
     */
    protected abstract void stopInternal();

    @Override
    public final void addSettingStateListener( SettingStateListener l ) {
        listeners.add( l );
    }

    @Override
    public final void removeSettingStateListener( SettingStateListener l ) {
        listeners.remove( l );
    }

    /**
     * An internal method used to notify all registered listeners that the setting
     * state has changed.
     *
     * @param settingEnabled the new state.
     */
    protected void fireSettingStateChanged( boolean settingEnabled ) {
        for (SettingStateListener l : listeners) {
            try {
                l.settingStateChanged( settingEnabled );
            } catch (Throwable ex) {
                log.throwing( getClass().getName(), "fireSettingStateChanged", ex );
            }
        }
    }

}
