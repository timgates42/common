package com.tim.net.handler;

import com.tim.io.VFS;
import java.net.*;

public class VFSHandlerFactory implements URLStreamHandlerFactory {

    private VFSHandler handler;
    
    public VFSHandlerFactory(VFS vfs) {
        this.handler = new VFSHandler(vfs);
    }
    
    public URLStreamHandler createURLStreamHandler(String protocol) {
        System.out.println("proto is " + protocol);
        if(protocol == "vfs") {
            return handler;
        }
        return null;
    }

}
