package com.tim.io;

import java.io.*;
import java.util.*;

public class VFSUtils {

    protected static final Integer ZERO = new Integer(0);
    protected static final Integer ONE = new Integer(1);
    protected static final Integer TWO = new Integer(2);
        
    public static void traverse(VFS vfs, String path, VFSTraverser delegate) throws IOException {
        fixedTraverse(vfs, fixPath(path), delegate);
    }
    
    private static void fixedTraverse(VFS vfs, String path, VFSTraverser delegate) throws IOException {
        if(!vfs.exists(path)) {
            return;
        }
        delegate.execute(path);
        if(!vfs.isDirectory(path)) {
            return;
        }
        String[] children = vfs.list(path);
        for(int i = 0; i < children.length; i++) {
            fixedTraverse(vfs, path + VFile.separator + children[i], delegate);
        }
    }
    
    public static String fixPath(String path) {
        if(path.startsWith(VFile.separator) && !path.endsWith(VFile.separator) &&
                path.indexOf(VFile.separator+VFile.separator) == -1) {
            return path;
        }
        StringBuffer result = new StringBuffer();
        for(int i = 0; i < path.length();) {
            for(;i < path.length(); i++) {
                if(path.charAt(i) != VFile.separatorChar) {
                    break;
                }
            }
            if(i == path.length()) {
                break;
            }
            int end = path.indexOf(VFile.separatorChar, i);
            if(end == -1) {
                end = path.length();
            }
            result.append(VFile.separator);
            result.append(path.substring(i, end));
            i = end + 1;
        }
        return result.toString();
    }
    
    public static final String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if(index == -1) {
            return "";
        }
        return filename.substring(index+1);
    }
    
    public static String getParent(String path) {
        int index = path.lastIndexOf(VFile.separatorChar);
        if(index == -1) {
            return VFile.separator;
        }
        return path.substring(0, index);
    }
    
    public static String getFileName(String path) {
        int index = path.lastIndexOf(VFile.separatorChar);
        if(index == -1) {
            return VFile.separator;
        }
        return path.substring(index+1);
    }
    
    public static String getUniqueFile(VFS vfs, String fileprefix, String filesuffix) {
        int low = 1;
        int high = 1;
        while(true) {
            if(!vfs.exists(fileprefix + high + filesuffix)) {
                break;
            }
            low = high + 1;
            high *= 2;
        }
        int insertion_point = -1 - Collections.binarySearch(new FileSeeker(vfs, fileprefix, filesuffix, low, high), ONE);
        return fileprefix + (insertion_point + low) + filesuffix;
    }
    
    private static final class FileSeeker extends AbstractList {
        private VFS vfs;
        private String fileprefix;
        private String filesuffix;
        private int low;
        private int high;
        public FileSeeker(VFS vfs, String fileprefix, String filesuffix, int low, int high) {
            this.vfs = vfs;
            this.fileprefix = fileprefix;
            this.filesuffix = filesuffix;
            this.low = low;
            this.high = high;
        }
        public Object get(int index) {
            if(vfs.exists(fileprefix + (index + low) + filesuffix)) {
                return ZERO;
            }
            return TWO;
        }
        public int size() {
            return high - low + 1;
        }
    }

}
