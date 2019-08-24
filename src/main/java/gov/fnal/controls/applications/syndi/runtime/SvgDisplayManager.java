// (c) 2001-2010 Fermi Research Allaince
// $Id: SvgDisplayManager.java,v 1.4 2010/09/15 18:43:05 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import gov.fnal.controls.applications.syndi.runtime.daq.DaqInterfaceFactory;
import gov.fnal.controls.applications.syndi.repository.RepositoryAccess;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  
 * @author  Andrey Petrov, Tim Bolshakov
 * @version $Revision: 1.4 $
 */
public class SvgDisplayManager {

    public static final int WORK_THREAD_COUNT =
            Integer.getInteger( "Synoptic.work-threads", 1 );
            
    public static final int MAX_IDLE_TIME =
            Integer.getInteger( "Synoptic.display-idle-time", 30000 );

    public static final int TASK_TIMEOUT =
            Integer.getInteger( "Synoptic.task-timeout", 10000 );

    public static final int VERSION_CHECK_RATE =
            Integer.getInteger( "Synoptic.version-check-rate", -1 );

    public static final int SHUTDOWN_TIMEOUT =
            Integer.getInteger( "Synoptic.shutdown-timeout", 10000 );

    public static final boolean QUARANTINE_ON_FAILURE =
            Boolean.getBoolean( "Synoptic.quarantine-on-failure" );

    public static final boolean RESTART_ON_FAILURE =
            Boolean.getBoolean( "Synoptic.restart-on-failure" );

    private static final Logger log = Logger.getLogger( SvgDisplayManager.class.getName());

    static {

        if (WORK_THREAD_COUNT < 1) {
            throw new Error( "Illegal work thread count" );
        }
        if (MAX_IDLE_TIME < 10000) {
            throw new Error( "Illegal display idle time" );
        }
        if (VERSION_CHECK_RATE < 10000) {
            throw new Error( "Illegal version check rate" );
        }
        if (SHUTDOWN_TIMEOUT < 1) {
            throw new Error( "Illegal shutdown timeout" );
        }

        StringBuilder buf = new StringBuilder( "SVG Display Manager Properties:" );
        buf.append( "\n        WORK_THREAD_COUNT: " );
        buf.append( WORK_THREAD_COUNT );
        buf.append( "\n            MAX_IDLE_TIME: " );
        buf.append( MAX_IDLE_TIME );
        buf.append( "\n             TASK_TIMEOUT: " );
        buf.append( TASK_TIMEOUT );
        buf.append( "\n       VERSION_CHECK_RATE: " );
        buf.append( VERSION_CHECK_RATE );
        buf.append( "\n         SHUTDOWN_TIMEOUT: " );
        buf.append( SHUTDOWN_TIMEOUT );
        buf.append( "\n    QUARANTINE_ON_FAILURE: " );
        buf.append( QUARANTINE_ON_FAILURE );
        buf.append( "\n       RESTART_ON_FAILURE: " );
        buf.append( RESTART_ON_FAILURE );
        log.config( buf.toString());

    }

    private final Map<String,SvgDisplay> displays = new HashMap<String,SvgDisplay>(); // name -> display
    private final RepositoryAccess repo;
    private final ScheduledExecutorService monitor = Executors.newSingleThreadScheduledExecutor();
    private final SvgDisplayBroker broker;
    private final Quarantine quarantine;
    private final AtomicInteger startCount = new AtomicInteger();
    private final AtomicInteger stopCount = new AtomicInteger();
    private final AtomicInteger abortCount = new AtomicInteger();

    public SvgDisplayManager( RepositoryAccess repo, Quarantine quarantine ) {
        
        if (repo == null) {
            throw new NullPointerException();
        }

        this.repo = repo;
        this.quarantine = quarantine;
        
        broker = new SvgDisplayBroker( WORK_THREAD_COUNT );

        broker.scheduleAtFixedRate(
            new ActivityChecker(),
            MAX_IDLE_TIME,
            MAX_IDLE_TIME / 2,
            TimeUnit.MILLISECONDS
        );

        if (TASK_TIMEOUT > 0) {
            monitor.scheduleAtFixedRate( 
                new TimeoutChecker(), 
                TASK_TIMEOUT,
                TASK_TIMEOUT / 2,
                TimeUnit.MILLISECONDS 
            );
        }
        
    }
    
    public String getDisplayTitle( String dispName ) { // TODO
        int i = dispName.lastIndexOf( "/" );
        if (i >= 0 && i < dispName.length()-1) {
            return dispName.substring( i + 1 );
        } else {
            return dispName;
        }
    }
    
    public synchronized SvgDisplay getDisplay( String name, String params ) {
        String dispId = (params == null) ? name : name + "(" + params + ")";
        SvgDisplay disp = displays.get( dispId );
        if (disp == null) {
            disp = new SvgDisplay( name, params );
            displays.put( dispId, disp );
            log.fine( "Scheduling launch of " + dispId );
            if (VERSION_CHECK_RATE > 0) {
                Launcher launcher = new Launcher( disp );
                ScheduledFuture<?> future = broker.scheduleAtFixedRate(
                    launcher,
                    0,
                    VERSION_CHECK_RATE,
                    TimeUnit.MILLISECONDS
                );
                launcher.setFuture( future );
            } else {
                broker.submit( new Launcher( disp ));
            }
        }
        return disp;
    }

    public synchronized int getDisplayCount() {
        return displays.size();
    }

    public int getStartCountAndReset() {
        return startCount.getAndSet( 0 );
    }

    public int getStopCountAndReset() {
        return stopCount.getAndSet( 0 );
    }

    public int getAbortCountAndReset() {
        return abortCount.getAndSet( 0 );
    }

    public void dispose() {

        monitor.shutdown();
        broker.shutdown();

        try {
            monitor.awaitTermination( 1, TimeUnit.SECONDS );
        } catch (InterruptedException ex) {
            log.throwing( getClass().getName(), "destroy", ex );
        }

        try {
            broker.awaitTermination( SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS );
        } catch (InterruptedException ex) {
            log.throwing( getClass().getName(), "destroy", ex );
        }

        for (SvgDisplay disp : displays.values()) {
            disp.dispose();
        }
        displays.clear();

        DaqInterfaceFactory.shutOffSharedInstance();

    }
    
    private class Launcher implements Runnable {
        
        final SvgDisplay disp;

        ScheduledFuture<?> future;
        
        Launcher( SvgDisplay disp ) {
            this.disp = disp;
        }
        
        @Override
        public void run() {
            broker.setCurrentTaskName( "{LAUNCH " + disp.getName() + "}" );
            broker.setCurrentDisplay( disp );
            if (disp.isDisposed()) {
                cancelTask();
                return;
            }
            String name = disp.getName();
            try {
                if (!repo.isDisplay( name )) {
                    throw new FileNotFoundException( name );
                }
                if (!disp.isRunning() && quarantine != null && quarantine.isQuarantined( name )) {
                    log.info( "Display " + name + " is broken" );
                    disp.stop( "Display Is Broken" );
                    return;
                }
                // The following method returns a document only if a version in the
                // repository is newer than a version currently in the display.
                // If the display is currently not running, its getVersion() return -1,
                // so the document always gets loaded.
                TimedDocument doc = repo.load( name, disp.getVersion());
                if (doc != null) {
                    if (disp.isRunning()) {
                        log.info( "Detected version change of " + disp );
                        disp.stop( "Restarting" );
                        disp.start( doc ); 
                    } else {
                        log.fine( "Loaded display " + disp );
                        startCount.incrementAndGet();
                        disp.start( doc ); 
                    }
                } else {
                    log.finer( "Version not changed for " + disp );
                }
                return;
            } catch (FileNotFoundException ex) {
                log.info( "Source not found for " + disp );
                disp.stop( "Display Not Found" );
            } catch (IOException ex) {
                log.log( Level.SEVERE, "Cannot load " + disp, ex );
                disp.stop( "Cannot Load Display" );
            }
            // Only if there is an error
            log.fine( "Scheduling disposal of " + disp );
            broker.schedule( 
                new Disposer( disp ), 
                MAX_IDLE_TIME,
                TimeUnit.MILLISECONDS 
            );
        }

        void setFuture( ScheduledFuture<?> future ) {
            this.future = future;
        }

        void cancelTask() {
            ScheduledFuture<?> f = this.future;
            if (f == null) {
                return;
            }
            if (f.cancel( false )) {
                log.fine( "Cancelled the launcher for " + disp );
            } else {
                log.warning( "Cannot cancel the launcher for " + disp );
            }
        }
        
    }
    
    private class Disposer implements Runnable {
        
        final SvgDisplay disp;
        
        Disposer( SvgDisplay disp ) {
            this.disp = disp;
        }

        @Override
        public void run() {
            broker.setCurrentTaskName( "{DISPOSE " + disp.getName() + "}" );
            broker.setCurrentDisplay( disp );
            boolean existed;
            synchronized (SvgDisplayManager.this) {
                existed = displays.values().remove( disp );
            }
            if (existed) {
                log.fine( "Disposing " + disp );
                disp.dispose();
                stopCount.incrementAndGet();
            }
            System.gc();
        }
        
    }

    private class TimeoutChecker implements Runnable {
        
        private final Set<FutureTask<?>> tardy =  new HashSet<FutureTask<?>>();
        private final Set<FutureTask<?>> stalled =  new HashSet<FutureTask<?>>();
        
        @Override
        public void run() {
            
            log.finer( "Checking tardy tasks" );
            
            Set<FutureTask<?>> tardy_new = broker.getTardyTasks( TASK_TIMEOUT );
            for (FutureTask<?> t : tardy_new) {
                if (tardy.contains( t )) {
                    
                    if (stalled.contains( t )) {
                        continue;
                    }
                    stalled.add( t );
                    
                    StringBuilder stackTrace = new StringBuilder( "Task stalled: " );
                    stackTrace.append( broker.getTaskName( t ));
                    stackTrace.append( "\nStack trace of the worker thread:" );
                    broker.appendStackTrace( t, stackTrace );
                    
                    log.severe( stackTrace.toString());
                    
                    SvgDisplay disp = broker.getDisplay( t );

                    if (disp != null) {
                        Mailer.sendMessage(
                                disp.getName(),
                                stackTrace,
                                QUARANTINE_ON_FAILURE && quarantine != null,
                                RESTART_ON_FAILURE
                        );
                    }
                    
                    if (disp != null && QUARANTINE_ON_FAILURE && quarantine != null) {
                        log.info( "Sending " + disp + " in quarantine" );
                        quarantine.quarantine( disp.getName());
                    }
                    
                    if (RESTART_ON_FAILURE) {
                        Restarter.restartServer();
                    }
                    
                } else if (!t.isDone()) {
                    try {
                        log.info( "Attempting to interrupt tardy task: " + broker.getTaskName( t ));
                        t.cancel( true );
                        abortCount.incrementAndGet();
                    } catch (Throwable ex) {
                        log.throwing( getClass().getName(), "run", ex );
                    }
                }
            }
            tardy.clear();
            tardy.addAll( tardy_new );
        }
        
    }

    private class ActivityChecker implements Runnable {

        @Override
        public void run() {
            broker.setCurrentTaskName( "{ACTIVITY CHECK}" );
            synchronized (SvgDisplayManager.this) {
                for (SvgDisplay disp : displays.values()) {
                    if (MAX_IDLE_TIME - disp.getIdleTime() <= 0) {
                        log.fine( "Scheduling disposal of " + disp );
                        broker.submit( new Disposer( disp ));
                    }
                }
            }
        }

    }

}
