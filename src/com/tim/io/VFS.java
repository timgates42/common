package com.tim.io;

import java.io.*;
import java.net.*;

public interface VFS {

    boolean delete(String path);
    boolean exists(String path);
    String getName(String path);
    boolean isDirectory(String path);
    boolean isFile(String path);
    String join(String path, String file);
    long lastModified(String path);
    long length(String path);
    String[] list(String path) throws IOException;
    boolean mkdir(String path);
    void move(String frompath, String topath) throws IOException;
    void copy(String frompath, String topath) throws IOException;
    InputStream read(String path) throws IOException;
    OutputStream write(String path) throws IOException;
    void close();
    void mount(String path, VFS vfs) throws IOException;
    void unmount(String path) throws IOException;
    VFSFile getRoot();
    URL getURL(String path);
    boolean supportsRandomAccess(String path);
    VFSRandomAccessFile openRandomAccessFile(String path) throws IOException;

}
