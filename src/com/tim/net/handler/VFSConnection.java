package com.tim.net.handler;

import com.tim.io.VFS;
import java.io.*;
import java.net.*;

public class VFSConnection extends URLConnection {

    private VFS vfs;
    private String path;
    
    public VFSConnection(VFS vfs, URL url) {
        super(url);
        this.vfs = vfs;
        this.path = url.getPath();
    }

    public void connect() throws IOException {
        this.connected = true;
    }
    
    public OutputStream getOutputStream() throws IOException {
        System.out.println("write " + path);
        return vfs.write(path);
    }

    public InputStream getInputStream() throws IOException {
        System.out.println("read " + path);
        return vfs.read(path);
    }

}
