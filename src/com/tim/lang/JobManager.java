package com.tim.lang;

import com.tim.adt.Heap;
import com.tim.adt.HeapElement;
import java.util.logging.Logger;

public class JobManager implements Runnable {

    private static final String NAME = "JobManager";
    private static final Logger log = Logger.getLogger(NAME);
    
    private int active_threads;
    private boolean remains_active;
    private int sequence_number;
    private Heap jobs;

    public JobManager() {
        this(1);
    }

    public JobManager(int threads) {
        this(threads, true);
    }
    
    public JobManager(boolean remains_active) {
        this(0, remains_active);
    }
    
    public JobManager(int threads, boolean remains_active) {
        this.remains_active = remains_active;
        jobs = new Heap();
        active_threads = threads;
        for(int i = 0; i < threads; i++) {
            (new Thread(this)).start();
        }
    }
    
    public void addJob(Runnable job) {
        addJobWithDelay(job, 0);
    }

    public void addJobWithDelay(Runnable job, long delay) {
        addJobAtTime(job, delay + System.currentTimeMillis());
    }
    
    public void addJobAtTime(Runnable job, long time) {
        synchronized(jobs) {
            jobs.insert(new JobDetails(job, time, sequence_number++));
            jobs.notifyAll();
            if(active_threads == 0 && !remains_active) {
                active_threads = 1;
                (new Thread(this)).start();
            }
        }
    }
    
    public int size() {
        return jobs.size();
    }
    
    public void run() {
        if(PerformanceManager.ACTIVE) {
            PerformanceManager.start(NAME);
        }
        try {
            while(true) {
                JobDetails job;
                synchronized(jobs) {
                    job = (JobDetails) jobs.peek();
                    if(job==null) {
                        if(PerformanceManager.ACTIVE) {
                            PerformanceManager.stop(NAME);
                        }
                        if(!remains_active) {
                            active_threads--;
                            return;
                        }
                        jobs.wait();
                        if(PerformanceManager.ACTIVE) {
                            PerformanceManager.start(NAME);
                        }
                        continue;
                    }
                    long time = job.getTime();
                    long ctime = System.currentTimeMillis();
                    if(time > ctime) {
                        if(PerformanceManager.ACTIVE) {
                            PerformanceManager.stop(NAME);
                        }
                        jobs.wait(time-ctime);
                        if(PerformanceManager.ACTIVE) {
                            PerformanceManager.start(NAME);
                        }
                        continue;
                    }
                    jobs.pop();
                }
                job.getJob().run();
            }
        } catch(InterruptedException ie) {
        }
    }
    
    private static final class JobDetails implements HeapElement {
        private int sequence;
        private long time;
        private Runnable job;
        public JobDetails(Runnable job, long time, int sequence) {
            this.sequence = sequence;
            this.job = job;
            this.time = time;
        }
        public int getSequence() {
            return sequence;
        }
        public long getTime() {
            return time;
        }
        public boolean lessThan(Object other) {
            long otime = ((JobDetails) other).getTime();
            int osequence = ((JobDetails) other).getSequence();
            return time < otime || (otime == time && sequence < osequence);
        }
        public boolean greaterThan(Object other) {
            long otime = ((JobDetails) other).getTime();
            int osequence = ((JobDetails) other).getSequence();
            return time > otime || (otime == time && sequence > osequence);
        }
        public Runnable getJob() {
            return job;
        }
    }
    
}
