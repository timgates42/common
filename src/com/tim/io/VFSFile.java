package com.tim.io;

import java.io.*;
import java.util.*;

public abstract class VFSFile {
    
    public Hashtable mount_points;
    
    public VFSFile() {
        mount_points = new Hashtable();
    }
    
    public VFSFile getMountChild(String name, boolean create) throws FileNotFoundException {
        VFS vfs = (VFS) mount_points.get(name);
        if(vfs != null) {
            return vfs.getRoot();
        }
        return getChild(name, create);
    }
    
    public void mount(String name, VFS vfs) throws IOException {
        if(mount_points.containsKey(name)) {
            throw new IOException(name + " aleady a mount point.");
        }
        mount_points.put(name, vfs);
    }
    
    public void unmount(String name) throws IOException {
        if(mount_points.remove(name) == null) {
            throw new IOException(name + " is not a mount point.");
        }
    }
    
    public String[] list() {
        HashSet set = new HashSet(listChildren());
        set.addAll(Collections.list(mount_points.keys()));
        String[] data = new String[set.size()];
        set.toArray(data);
        return data;
    }
    
    public boolean supportsRandomAccess() {
        return false;
    }

    public VFSRandomAccessFile openRandomAccessFile() throws IOException {
        return null;
    }
    
    public abstract InputStream read() throws IOException;
    public abstract OutputStream write() throws IOException;
    public abstract VFSFile getChild(String name, boolean create) throws FileNotFoundException;
    public abstract List listChildren();
    public abstract boolean mkdir(String name);
    public abstract boolean move(VFSFile src, String srcname, VFSFile dstdir, String dstname);
    public abstract boolean delete(String name);
    public abstract String getName();
    public abstract long lastModified();
    public abstract long length();
    public abstract boolean isDirectory();
    public abstract String getFileSystemType();
    public abstract boolean canMoveFilesTo(VFSFile dir);
    
}
