package com.tim.net.handler;

import java.util.*;
import java.net.*;

public class DefaultHandlerFactory implements URLStreamHandlerFactory {

    private static DefaultHandlerFactory INSTANCE;

    private Hashtable factories;
    
    private DefaultHandlerFactory() {
        factories = new Hashtable();
    }
    
    public URLStreamHandler createURLStreamHandler(String protocol) {
        System.out.println("proto is " + protocol);
        if(protocol == null) {
            return null;
        }
        URLStreamHandlerFactory factory = (URLStreamHandlerFactory) factories.get(protocol);
        if(factory == null) {
            return null;
        }
        return factory.createURLStreamHandler(protocol);
    }
    
    public synchronized static void addHandler(String protocol, URLStreamHandlerFactory factory) {
        if(INSTANCE == null) {
            INSTANCE = new DefaultHandlerFactory();
            URL.setURLStreamHandlerFactory(INSTANCE);
        }
        INSTANCE.put(protocol, factory);
    }
    
    private void put(String protocol, URLStreamHandlerFactory factory) {
        factories.put(protocol, factory);
    }

}
