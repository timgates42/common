package com.tim.util;

import java.util.logging.*;
import java.util.*;
import java.io.*;

import com.tim.lang.StringUtils;

public class Logging {
    
    public static void clearLoggingConfig() {
        Logger root = Logger.getLogger("");
        
        // Remove old handlers
        Handler[] oldhandlers = root.getHandlers();
        for (int i = 0; i < oldhandlers.length; i++) {
            root.removeHandler(oldhandlers[i]);
        }
    }
    
    public static void basicConfig() {
        basicConfig(new ConsoleHandler());
    }
    
    public static void basicConfig(File file) throws IOException {
        basicConfig(new FileHandler(file.toString()));
    }
    
    public static void basicConfig(Handler handler) {
        clearLoggingConfig();
        
        // Add simple stdout logging
        Logger root = Logger.getLogger("");
        root.setLevel(Level.ALL);
        handler.setFormatter(new FlatlineFormatter());
        handler.setLevel(Level.ALL);
        root.addHandler(handler);
        
        hideSunLogging();
    }
        
    public static void hideSunLogging() {
        Logger sunlogger = Logger.getLogger("sun.rmi.loader");
        sunlogger.setLevel(Level.WARNING);
    }
    
    public static final class FlatlineFormatter extends java.util.logging.Formatter {
        public String format(LogRecord record) {
            Calendar cal = new GregorianCalendar();
            String mon = "Jan";
            cal.setTimeInMillis(record.getMillis());

            switch (cal.get(Calendar.MONTH)) {
                case Calendar.JANUARY:   mon = "Jan"; break;
                case Calendar.FEBRUARY:  mon = "Feb"; break;
                case Calendar.MARCH:     mon = "Mar"; break;
                case Calendar.APRIL:     mon = "Apr"; break;
                case Calendar.MAY:       mon = "May"; break;
                case Calendar.JUNE:      mon = "Jun"; break;
                case Calendar.JULY:      mon = "Jul"; break;
                case Calendar.AUGUST:    mon = "Aug"; break;
                case Calendar.SEPTEMBER: mon = "Sep"; break;
                case Calendar.OCTOBER:   mon = "Oct"; break;
                case Calendar.NOVEMBER:  mon = "Nov"; break;
                case Calendar.DECEMBER:  mon = "Dec"; break;
            }

            Throwable ex = record.getThrown();
            String exstr = "";
            if (ex != null) {
                StringWriter out = new StringWriter();
                out.write("    ");
                ex.printStackTrace(new PrintWriter(out));
                exstr = out.toString();
            }
            return mon + " " + 
                StringUtils.zpad(cal.get(Calendar.DAY_OF_MONTH), 2) + " " + 
                StringUtils.zpad(cal.get(Calendar.HOUR_OF_DAY), 2) + ":" + 
                StringUtils.zpad(cal.get(Calendar.MINUTE), 2) + ":" + 
                StringUtils.zpad(cal.get(Calendar.SECOND), 2) + " " + 
                record.getLoggerName() + "." + 
                record.getSourceClassName() + "." +
                record.getSourceMethodName() + "[" + 
                record.getThreadID() + "]: " +
                record.getMessage() + "\n" + exstr;
        }
    }

}
