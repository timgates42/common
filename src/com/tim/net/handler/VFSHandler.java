package com.tim.net.handler;

import com.tim.io.VFS;
import java.net.*;

public class VFSHandler extends URLStreamHandler {

    private VFS vfs;
    
    public VFSHandler(VFS vfs) {
        this.vfs = vfs;
    }
    
    public URLConnection openConnection(URL url) {
        System.out.println("connect " + url);
        return new VFSConnection(vfs, url);
    }

}
