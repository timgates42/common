package com.tim.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.util.Collections;
import java.util.List;

public class MemoryVFS extends AbstractVFS {

    public static final String MEMORY_FILE_SYSTEM_TYPE = "MemoryFS";
    
    private MemoryVFSFile root;
    
    public MemoryVFS() {
        root = new MemoryVFSFile("", true);
    }
    
    public VFSFile getRoot() {
        return root;
    }
    
    public void close() {
        root.dispose();
        root = null;
    }
    
    private static final class MemoryVFSFile extends VFSFile {
    
        private boolean is_directory;
        private String name;
        private long timestamp;
        private Hashtable children;
        private byte[] data;
        
        public MemoryVFSFile(String name, boolean is_directory) {
            this.name = name;
            this.is_directory = is_directory;
            timestamp = System.currentTimeMillis();
            if(is_directory) {
                children = new Hashtable();
            } else {
                data = new byte[0];
            }
        }
        
        public InputStream read() throws FileNotFoundException {
            if(isDirectory()) {
                throw new FileNotFoundException("File is a directory");
            }
            return new ByteArrayInputStream(data);
        }
        
        public OutputStream write() throws FileNotFoundException {
            if(isDirectory()) {
                throw new FileNotFoundException("File is a directory");
            }
            return new MemoryVFSFileOutputStream(this);
        }
        
        public VFSFile getChild(String name, boolean create) throws FileNotFoundException {
            if(!isDirectory()) {
                throw new FileNotFoundException(name);
            }
            boolean exists = children.containsKey(name);
            if(!exists) {
                if (!create) {
                    throw new FileNotFoundException(name);
                } else {
                    MemoryVFSFile file = new MemoryVFSFile(name, false);
                    children.put(name, file);
                    return file;
                }
            }
            return (VFSFile) children.get(name);
        }
        
        public List listChildren() {
            return Collections.list(children.keys());
        }
        
        public boolean mkdir(String name) {
            if(!isDirectory() || children.containsKey(name)) {
                return false;
            }
            return children.put(name, new MemoryVFSFile(name, true)) == null;
        }
        
        public boolean canMoveFilesTo(VFSFile dir) {
            return dir.getFileSystemType().equals(MEMORY_FILE_SYSTEM_TYPE);
        }
        
        public boolean move(VFSFile src, String srcname, VFSFile dstdir, String dstname) {
            if(!((MemoryVFSFile) dstdir).create(dstname, src, true)) {
                return false;
            }
            return unlink(name);
        }
        
        private boolean unlink(String name) {
            if(!children.containsKey(name)) {
                return false;
            }
            return children.remove(name) != null;
        }
        
        public boolean delete(String name) {
            if(!children.containsKey(name)) {
                return false;
            }
            MemoryVFSFile file = (MemoryVFSFile) children.remove(name);
            file.dispose();
            return true;
        }
        
        public String getName() {
            return name;
        }
        
        public long lastModified() {
            return timestamp;
        }
        
        public long length() {
            if(isDirectory()) {
                return 0;
            }
            return data.length;
        }
        
        public boolean isDirectory() {
            return is_directory;
        }
        
        public void setData(byte[] data) {
            timestamp = System.currentTimeMillis();
            this.data = data;
        }
        
        public boolean supportsRandomAccess() {
            return true;
        }
    
        public VFSRandomAccessFile openRandomAccessFile() throws FileNotFoundException {
            if(isDirectory()) {
                throw new FileNotFoundException("Unable to open file as it is a directory.");
            }
            return new MemoryRandomAccessFile(this, data);
        }
    
        public void dispose() {
            String[] elems = list();
            for(int i = 0; i < elems.length; i++) {
                delete(elems[i]);
            }
        }

        private boolean create(String name, VFSFile src, boolean override) {
            if(!isDirectory() || (!override && children.containsKey(name))) {
                return false;
            }
            MemoryVFSFile old = (MemoryVFSFile) children.put(name, src);
            if(old != null) {
                old.dispose();
            }
            return true;
        }
        
        public String getFileSystemType() {
            return MEMORY_FILE_SYSTEM_TYPE;
        }
    }
    
    private static final class MemoryVFSFileOutputStream extends ByteArrayOutputStream {
        
        private MemoryVFSFile file;
        
        public MemoryVFSFileOutputStream(MemoryVFSFile file) {
            this.file = file;
        }
        
        public void close() throws IOException {
            super.close();
            file.setData(toByteArray());
        }
        
    }

    public static final class MemoryRandomAccessFile implements VFSRandomAccessFile {
        
        private MemoryVFSFile base;
        private byte[] buffer;
        private int position;
        
        public MemoryRandomAccessFile(MemoryVFSFile base, byte[] buffer) {
            this.base = base;
            this.buffer = buffer;
            position = 0;
        }
        
        public void seek(long len) {
            position = (int) len;
        }
        
        public void skip(int len) {
            position += (int) len;
        }
        
        public int read(byte[] data, int offset, int len) {
            int avail = Math.min(buffer.length - position, len);
            System.arraycopy(buffer, position, data, offset, avail);
            position += avail;
            return avail;
        }
        
        public void write(byte[] data, int offset, int len) {
            if(buffer.length >= position + len) {
                System.arraycopy(data, offset, buffer, position, len);
            } else {
                byte[] temp = new byte[position+len];
                if(position > 0) {
                    System.arraycopy(buffer, 0, temp, 0, position);
                }
                System.arraycopy(data, offset, temp, position, len);
                buffer = temp;
            }
            position += len;
        }
        
        public void setLength(long len) {
            if(len != buffer.length) {
                byte[] temp = new byte[(int)len];
                System.arraycopy(buffer, 0, temp, 0, (int)len);
                buffer = temp;
            }
        }
        
        public long tell() {
            return position;
        }
        
        public void close() throws IOException {
            base.setData(buffer);
        }
        
        public long length() {
            return buffer.length;
        }
    
    }
}
