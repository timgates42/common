package com.tim.io;

import java.io.*;
import java.util.*;

public class FlatFileVFS extends AbstractVFS {

    public static final String FLAT_FILE_SYSTEM_TYPE = "RegularFS";
    
    private FlatFileVFSFile root;
    
    public FlatFileVFS(File dir) {
        root = new FlatFileVFSFile(dir);
    }
    
    public VFSFile getRoot() {
        return root;
    }
    
    public void close() {
        root = null;
    }
    
    private static final class FileInputStreamWrap extends FileInputStream{
        private File file;
        public FileInputStreamWrap(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
            System.out.println(file + " is open for Input.");
            System.out.flush();
        }
        public void close() throws IOException {
            super.close();
            System.out.println(file + " is closed for Input.");
            System.out.flush();
        }
    }
    
    private static final class FileOutputStreamWrap extends FileOutputStream {
        private FileOutputStream stream;
        private File file;
        public FileOutputStreamWrap(File file) throws FileNotFoundException {
            super(file);
            this.file = file;
            System.out.println(file + " is open for Output.");
            System.out.flush();
        }
        public void close() throws IOException {
            super.close();
            System.out.println(file + " is closed for Output.");
            System.out.flush();
        }
    }
    
    private static final class FlatFileVFSFile extends VFSFile {
    
        private File file;
        
        public FlatFileVFSFile(File file) {
            this.file = file;
        }
        
        public InputStream read() throws FileNotFoundException {
            return new FileInputStream(file);
        }
        
        public OutputStream write() throws FileNotFoundException {
            return new FileOutputStream(file);
        }
        
        public VFSFile getChild(String name, boolean create) throws FileNotFoundException {
            File target = new File(file, name);
            if(!target.exists() && !create) {
                throw new FileNotFoundException(name);
            }
            FlatFileVFSFile result = new FlatFileVFSFile(target);
            return result;
        }
        
        public List listChildren() {
            String[] list = file.list();
            if(list == null) {
                return new Vector();
            }
            List result = Arrays.asList(list);
            return result;
        }
        
        public boolean mkdir(String name) {
            File target = new File(file, name);
            boolean result = target.mkdir();
            return result;
        }
        
        public boolean delete(String name) {
            File target = new File(file, name);
            boolean result = FileUtils.delete(target);
            return result;
        }
        
        public String getName() {
            String result = file.getName();
            return result;
        }
        
        public long lastModified() {
            long result;
            if(checkMountPoint()) {
                result = 0;
            } else {
                result = file.lastModified();
            }
            return result;
        }
        
        public long length() {
            long result;
            if(checkMountPoint()) {
                result = 0;
            } else {
                result = file.length();
            }
            return result;
        }
        
        public boolean isDirectory() {
            boolean result;
            if(checkMountPoint()) {
                result = true;
            } else {
                result = file.isDirectory();
            }
            return result;
        }
        
        private File getFile() {
            return file;
        }
        
        public boolean canMoveFilesTo(VFSFile dir) {
            return dir.getFileSystemType().equals(FLAT_FILE_SYSTEM_TYPE);
        }
        
        public boolean move(VFSFile src, String srcname, VFSFile dstdir, String dstname) {
            System.out.println("file move");
            File dst = new File(((FlatFileVFSFile) dstdir).getFile(), dstname);
            try {
                IOUtils.move(((FlatFileVFSFile) src).getFile(), dst);
                return true;
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }

        public boolean supportsRandomAccess() {
            return true;
        }
    
        public VFSRandomAccessFile openRandomAccessFile() throws FileNotFoundException {
            return new FlatRandomAccessFile(getFile());
        }
    
        public String getFileSystemType() {
            return FLAT_FILE_SYSTEM_TYPE;
        }
        
        private boolean checkMountPoint() {
             /* Don't check some operations if the file is a root mount on Windows as it
            can be slow */
            String path = file.toString();
            boolean result = path.indexOf(File.separatorChar) == path.length() - 1;
            return result;
        }
            
    }
    
    public static final class FlatRandomAccessFile implements VFSRandomAccessFile {
        
        private RandomAccessFile base;
        
        public FlatRandomAccessFile(File base) throws FileNotFoundException {
            this.base = new RandomAccessFile(base, "rws");
        }
        
        public void seek(long position) throws IOException {
            base.seek(position);
        }
        
        public void skip(int len) throws IOException {
            base.skipBytes(len);
        }
        
        public int read(byte[] data, int offset, int len) throws IOException {
            return base.read(data, offset, len);
        }
        
        public void write(byte[] data, int offset, int len) throws IOException {
            base.write(data, offset, len);
        }
        
        public void setLength(long len) throws IOException {
            base.setLength(len);
        }
        
        public long tell() throws IOException {
            return base.getFilePointer();
        }
        
        public void close() throws IOException {
            base.close();
        }
        
        public long length() throws IOException {
            return base.length();
        }
    
    }
}
