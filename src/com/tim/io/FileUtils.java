package com.tim.io;

import java.io.*;

public class FileUtils {
    public static boolean delete(File file) {
        return delete(file, true);
    }
    public static boolean delete(File file, boolean included) {
        boolean failure = false;
        if(file.isDirectory()) {
            String[] list = file.list();
            for(int i = 0; i < list.length; i++) {
                failure = ! delete(new File(file, list[i])) || failure;
            }
        }
        if(included) {
            return file.delete() && ! failure;
        }
        return file.exists() && ! failure;
    }
    
    public static void ensureBaseDir(File file) throws IOException {
        File parent = file.getParentFile();
        if(!parent.mkdirs()) {
            if(!parent.isDirectory()) {
                throw new IOException("Failed to create dir " + parent);
            }
        }
    }
    
    public static void ensureExists(File file) throws IOException {
        if(!file.exists()) {
            throw new IOException(file + " does not exist.");
        }
    }

    public static void ensureNotExists(File file) throws IOException {
        if(file.exists()) {
            throw new IOException(file + " already exists.");
        }
    }

    public static void ensureDelete(File file) throws IOException {
        boolean err = !file.delete();
        if(!err && file.exists()) {
            err = true;
        }
        if(err) {
            throw new IOException(file + " could not be removed.");
        }
    }

}
