package com.tim.io;

import java.io.*;

public class VFile {

    public static final String root = "/";
    public static final String separator = "/";
    public static final char separatorChar = '/';

    private VFS vfs;
    private String path;
    
    public VFile(VFS vfs, String path) {
        this.vfs = vfs;
        this.path = VFSUtils.fixPath(path);
    }
    
    public VFile(VFile parent, String filename) {
        this(parent.getVFS(), parent.getPath() + VFile.separator + filename);
    }
    
    public String[] list() {
        try {
            return vfs.list(path);
        } catch(IOException ioe) {
            return new String[0];
        }
    }
    
    public boolean supportsRandomAccess() {
        return vfs.supportsRandomAccess(path);
    }

    public VFSRandomAccessFile openRandomAccessFile() throws IOException {
        return vfs.openRandomAccessFile(path);
    }
    
    public InputStream read() throws IOException {
        return vfs.read(path);
    }
    
    public OutputStream write() throws IOException {
        return vfs.write(path);
    }
    
    public boolean mkdir() {
        return vfs.mkdir(path);
    }
    
    public boolean renameTo(VFile dst) {
        try {
            move(dst);
            return true;
        } catch(IOException ioe) {
            return false;
        }
    }
    
    public void move(VFile dst) throws IOException {
        vfs.move(path, dst.getPath());
    }
    
    public boolean delete() {
        return vfs.delete(path);
    }
    
    public String getName() {
        return vfs.getName(path);
    }
    
    public long lastModified() {
        return vfs.lastModified(path);
    }
    
    public long length() {
        return vfs.length(path);
    }
    
    public boolean exists() {
        return vfs.exists(path);
    }
    
    public boolean isDirectory() {
        return vfs.isDirectory(path);
    }

    public String getPath() {
        return path;
    }
    
    public VFS getVFS() {
        return vfs;
    }

}
