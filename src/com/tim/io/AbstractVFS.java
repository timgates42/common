package com.tim.io;

import com.tim.lang.StringUtils;
import java.io.*;
import java.net.*;
import java.util.*;

public abstract class AbstractVFS implements VFS {

    public String getName(String path) {
        String[] dir = splitPath(path);
        return dir[dir.length-1];
    }
    
    protected String[] splitPath(String path) {
        return StringUtils.split(path, VFile.separator, true);
    }
    
    public void move(String frompath, String topath) throws IOException {
        moveOrCopy(frompath, topath, true);
    }
    
    public void copy(String frompath, String topath) throws IOException {
        moveOrCopy(frompath, topath, false);
    }
         
    public void moveOrCopy(String frompath, String topath, boolean move) throws IOException {
        String srcfile = VFSUtils.getFileName(frompath);
        if(isDirectory(topath)) {
            topath = VFSUtils.fixPath(topath) + VFile.separator + srcfile; 
        }
        if(Arrays.asList(splitPath(frompath)).equals(Arrays.asList(splitPath(topath)))) {
            if(move) {
                return;
            }
            String prefix = VFSUtils.getParent(topath);
            String suffix = VFSUtils.getFileName(topath);
            int index = suffix.lastIndexOf('.');
            if(index == -1) {
                prefix = prefix + VFile.separator + suffix;
                suffix = "";
            } else {
                prefix = prefix + VFile.separator + suffix.substring(0, index);
                suffix = suffix.substring(index);
            }
            topath = VFSUtils.getUniqueFile(this, prefix, suffix);
        }
        System.err.println("movecopy " + frompath + " to " + topath);
        traverseMoveOrCopy(frompath, topath, move);
    }
    
    private void traverseMoveOrCopy(String srcpath, String destpath, boolean move) throws IOException {
        MoveOrCopyDelegate delegate = new MoveOrCopyDelegate(srcpath, destpath, move);
        VFSUtils.traverse(this, srcpath, delegate);
    }

    private final class MoveOrCopyDelegate implements VFSTraverser {
        private String srcbase;
        private String destbase;
        private boolean move;
        public MoveOrCopyDelegate(String srcbase, String destbase, boolean move) {
            this.srcbase = VFSUtils.fixPath(srcbase);
            this.destbase = VFSUtils.fixPath(destbase);
            this.move = move;
        }
        public void execute(String path) throws IOException {
            boolean isDir = isDirectory(path);
            String subpath = path.substring(srcbase.length());
            String target = destbase + subpath;
            if(isDir) {
                if(!exists(target)) {
                    if(!mkdir(target)) {
                        throw new IOException("Unable to create directory " + target);
                    }
                } else if(!isDirectory(target)) {
                    if(!delete(target)) {
                        throw new IOException("Unable to replace " + target);
                    }
                    if(!mkdir(target)) {
                        throw new IOException("Unable to create directory " + target);
                    }
                }
            } else {
                if(exists(target)) {
                    if(!delete(target)) {
                        throw new IOException("Unable to replace " + target);
                    }
                }
                moveOrCopyFile(path, target, move);
            }
        }
    }
    
    private void moveOrCopyFile(String frompath, String topath, boolean move) throws IOException {
        String[] frompathelem = splitPath(frompath);
        VFSFile srcdir = getParentDir(frompathelem);
        String srcfile = getFileName(frompathelem);
        String[] topathelem = splitPath(topath);
        VFSFile dstdir = getParentDir(topathelem);
        String dstfile = getFileName(topathelem);
        if(move && srcdir.canMoveFilesTo(dstdir)) {
            if(!srcdir.move(getFile(frompathelem), srcfile, dstdir, dstfile)) {
                throw new IOException("Unable to move files from " + frompath);
            }
        } else {
            IOUtils.copy(srcdir.getMountChild(srcfile, false).read(), dstdir.getMountChild(dstfile, true).write());
            if(move) {
                if(!srcdir.delete(dstfile)) {
                    throw new IOException("Unable to remove " + frompath + " after move.");
                }
            }
        }
    }
    
    public boolean delete(String path) {
        try {
            String[] pathelem = splitPath(path);
            return getParentDir(pathelem).delete(getFileName(pathelem));
        } catch(FileNotFoundException fnfe) {
            return false;
        }
    }
    
    public boolean exists(String path) {
        try {
            getFile(path);
            return true;
        } catch(FileNotFoundException fnfe) {
            return false;
        }
    }
    
    public boolean isDirectory(String path) {
        try {
            return getFile(path).isDirectory();
        } catch(FileNotFoundException fnfe) {
            return false;
        }
    }
    
    public boolean isFile(String path) {
        try {
            return ! getFile(path).isDirectory();
        } catch(FileNotFoundException fnfe) {
            return false;
        }
    }
    
    public String join(String path, String file) {
        return path + VFile.separator + file;
    }
    
    public long lastModified(String path) {
        try {
            return getFile(path).lastModified();
        } catch(FileNotFoundException fnfe) {
            return 0;
        }
    }
    
    public long length(String path) {
        try {
            return getFile(path).length();
        } catch(FileNotFoundException fnfe) {
            return 0;
        }
    }
    
    public String[] list(String path) throws IOException {
        VFSFile file = getFile(path);
        if(!file.isDirectory()) {
            throw new IOException(path + " is not a directory.");
        }
        return file.list();
    }
    
    public boolean mkdir(String path) {
        try {
            String[] pathelem = splitPath(path);
            return getParentDir(pathelem).mkdir(getFileName(pathelem));
        } catch(FileNotFoundException fnfe) {
            return false;
        }
    }
    
    public InputStream read(String path) throws IOException {
        return getFile(path).read();
    }
    
    public OutputStream write(String path) throws IOException {
        return getFile(path, true).write();
    }
    
    public boolean supportsRandomAccess(String path) {
        try {
            return getFile(path).supportsRandomAccess();
        } catch(FileNotFoundException fnfe) {
            return false;
        }
    }

    public VFSRandomAccessFile openRandomAccessFile(String path) throws IOException {
        return getFile(path, true).openRandomAccessFile();
    }
    
    public void mount(String path, VFS vfs) throws IOException {
        String[] pathelem = splitPath(path);
        VFSFile vfsfile = getParentDir(pathelem);
        vfsfile.mount(getFileName(pathelem), vfs);
    }
    
    public void unmount(String path) throws IOException {
        String[] pathelem = splitPath(path);
        VFSFile vfsfile = getParentDir(pathelem);
        vfsfile.unmount(getFileName(pathelem));
    }
    
    public String getFileName(String[] path) throws FileNotFoundException {
        if(path.length == 0) {
            throw new FileNotFoundException();
        }
        return path[path.length-1];
    }
    
    public VFSFile getParentDir(String[] path) throws FileNotFoundException {
        if(path.length == 0) {
            throw new FileNotFoundException();
        }
        return getFile(path, false, 0, path.length-1);
    }
    
    public VFSFile getFile(String path) throws FileNotFoundException {
        return getFile(path, false);
    }
    
    public VFSFile getFile(String path, boolean create) throws FileNotFoundException {
        return getFile(splitPath(path), create);
    }
    
    public VFSFile getFile(String[] path) throws FileNotFoundException {
        return getFile(path, false);
    }
    
    public VFSFile getFile(String[] path, boolean create) throws FileNotFoundException {
        return getFile(path, create, 0, path.length);
    }
    
    public VFSFile getFile(String[] path, boolean create, int base, int depth) throws FileNotFoundException {
        VFSFile file = getRoot();
        for(int i = base; i < base + depth; i++) {
            file = file.getMountChild(path[i], create && ((base+depth-1) == i));
        }
        return file;
    }
    
    public URL getURL(String path) {
        try {
            int i;
            for(i = 0; i < path.length(); i++) {
                if(path.charAt(i) != VFile.separatorChar) {
                    break;
                }
            }
            URL url = new URL("vfs:///" + path.substring(i));
            System.out.println(url.getPath());
            return url;
        } catch(MalformedURLException mue) {
            throw new RuntimeException(mue.getMessage());
        }
    }
    
}
