package com.tim.app;

import java.util.*;

public class ApplicationRegistry {
    
    private static final Hashtable HANDLERS = new Hashtable();
    
    public static ApplicationHandler getHandler(String filename) {
        int index = filename.lastIndexOf('.');
        if(index == -1) {
            return null;
        }
        String ext = filename.substring(index+1);
        return (ApplicationHandler) HANDLERS.get(ext.toLowerCase());
    }
    
    public static void registerHandler(String ext, ApplicationHandler app) {
        registerHandler(new String[] {ext}, app);
    }
    
    public static void registerHandler(String[] ext, ApplicationHandler app) {
        for(int i = 0; i < ext.length; i++) {
            HANDLERS.put(ext[i].toLowerCase(), app);
        }
    }
    
}
