// (c) 2001-2010 Fermi Research Allaince
// $Id: SvgDisplayBroker.java,v 1.2 2010/09/15 15:25:15 apetrov Exp $
package gov.fnal.controls.applications.syndi.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 *
 * @author Andrey Petrov
 * @version $Date: 2010/09/15 15:25:15 $
 */
class SvgDisplayBroker extends ScheduledThreadPoolExecutor {
    
    private final Map<FutureTask<?>,WorkerThread> running = new HashMap<FutureTask<?>,WorkerThread>();
    
    private long workTime = 0;
    private long startTime = System.currentTimeMillis();
    
    SvgDisplayBroker( int threadCount ) {
        super( threadCount, new WorkerThreadFactory(), new AbortPolicy());
        setContinueExistingPeriodicTasksAfterShutdownPolicy( false );
        setExecuteExistingDelayedTasksAfterShutdownPolicy( false );
        prestartAllCoreThreads();
    }
    
    synchronized Set<FutureTask<?>> getTardyTasks( long timeout ) {
        long t0  = System.currentTimeMillis() - timeout;
        Set<FutureTask<?>> res = new HashSet<FutureTask<?>>();
        for (FutureTask<?> task : running.keySet()) {
            if (running.get( task ).startTime < t0) {
                res.add( task );
            }
        }
        return res;
    }
    
    Appendable appendStackTrace( FutureTask<?> task, Appendable buf ) {
        Thread t;
        synchronized (this) {
            t = running.get( task );
        }
        if (t != null) {
            for (StackTraceElement e : t.getStackTrace()) {
                try {
                    buf.append( "\n    " );
                    buf.append( e.toString());
                } catch (IOException ex) {}
            }
        }
        return buf;
    }
    
    synchronized String getTaskName( FutureTask<?> task ) {
        WorkerThread t = running.get( task );
        return (t != null) ? t.taskName : null;
    }
    
    synchronized SvgDisplay getDisplay( FutureTask<?> task ) {
        WorkerThread t = running.get( task );
        return (t != null) ? t.display : null;
    }
    
    void setCurrentTaskName( String name ) {
        Thread t = Thread.currentThread();
        if (t instanceof WorkerThread) {
            ((WorkerThread)t).taskName = name;
        }
    }
    
    void setCurrentDisplay( SvgDisplay disp ) {
        Thread t = Thread.currentThread();
        if (t instanceof WorkerThread) {
            ((WorkerThread)t).display = disp;
        }
    }
    
    @Override
    protected synchronized void beforeExecute( Thread thread, Runnable task ) {
        WorkerThread wt = (WorkerThread)thread;
        wt.taskName = "Unnamed Task";
        wt.display = null;
        wt.startTime = wt.startTimeStat = System.currentTimeMillis();
        running.put( (FutureTask<?>)task, wt );
        super.beforeExecute( thread, task );
    }
    
    
    @Override
    protected synchronized void afterExecute( Runnable task, Throwable ex ) {
        WorkerThread wt = running.remove( (FutureTask<?>)task );
        if (wt != null) {
            wt.taskName = "Idle Task";
            wt.display = null;
            long t0 = System.currentTimeMillis();
            if (wt.startTimeStat < t0) {
                workTime += t0 - wt.startTimeStat;
            }
            wt.startTime = wt.startTimeStat = Long.MAX_VALUE;
        }
        super.afterExecute( task, ex );
    }
    

    synchronized int getWorkTime() {
        long t0 = System.currentTimeMillis();
        for (WorkerThread wt : running.values()) {
            if (wt.startTimeStat < t0) {
                workTime += t0 - wt.startTimeStat;
                wt.startTimeStat = t0;
            }
        }
        double totalTime = (t0 - startTime) * getCorePoolSize();
        int res = (int)(100.0 * workTime / totalTime);
        startTime = t0;
        workTime = 0;
        return res;
    }
    
    private static class WorkerThreadFactory implements ThreadFactory {
            
        private int cnt = 0;

        @Override
        public Thread newThread( Runnable target ) {
            return new WorkerThread( target, "Display Broker Thread #" + (cnt++) );
        }
            
    }
    
    private static class WorkerThread extends Thread {
        
        String taskName = null;
        SvgDisplay display = null;
        long startTime = Long.MAX_VALUE;
        long startTimeStat = Long.MAX_VALUE;
        
        public WorkerThread( Runnable target, String name ) {
            super( target, name );
        }
        
    }
    
}
