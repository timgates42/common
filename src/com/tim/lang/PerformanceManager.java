package com.tim.lang;

import java.util.*;

public class PerformanceManager {

    private static final Hashtable timers = new Hashtable();
    public static final boolean ACTIVE = true;
    private static final String MASTER_TIMER = "Master Timer";
    
    public static void init() {
        start(MASTER_TIMER);
    }
    
    public static void exit() {
        Timer master = get(MASTER_TIMER);
        master.stop();
        long total_time = master.getTime();
        timers.remove(MASTER_TIMER);
        Enumeration e = timers.elements();
        System.out.println("The Application ran for a total of " + total_time + " milliseconds.");
        while(e.hasMoreElements()) {
            Timer timer = (Timer) e.nextElement();
            System.out.println(timer.getDetails(total_time));
        }
    }
    
    public static void start(String key) {
        Timer timer = get(key);
        timer.start();
    }
    
    public static void stop(String key) {
        Timer timer = get(key);
        timer.stop();
    }
    
    public static Timer get(String key) {
        Timer timer = (Timer) timers.get(key);
        if(timer == null) {
            timer = new Timer(key);
            timers.put(key, timer);
        }
        return timer;
    }

    private static final class Timer {
        private long start_time;
        private long active_time;
        private String name;
        public Timer(String name) {
            this.name = name;
            start_time = -1;
        }
        public void start() {
            if(start_time == -1) {
                start_time = System.currentTimeMillis();
            }
        }
        public void stop() {
            if(start_time != -1) {
                active_time += System.currentTimeMillis() - start_time;
                start_time = -1;
            }
        }
        public long getTime() {
            return active_time;
        }
        public String getDetails(long total_time) {
            return name + " ran for " + active_time + " milliseconds (" + (active_time*100.0/total_time) + "%).";
        }
    }
   
}
