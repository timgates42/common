package com.tim.util;

import java.io.*;

public class DataTransfer {
    
    public static void copy(InputStream src, OutputStream dst) throws IOException {
        byte[] data = new byte[4096];
        int len;
        while((len = src.read(data)) != -1) {
            dst.write(data, 0, len);
        }
    }
    
    public static void copy(Reader src, OutputStream dst) throws IOException {
        copy(src, new OutputStreamWriter(dst));
    }
    

    public static void copy(InputStream src, Writer dst) throws IOException {
        copy(new InputStreamReader(src), dst);
    }
    
    public static void copy(Reader src, Writer dst) throws IOException {
        char[] data = new char[4096];
        int len;
        while((len = src.read(data)) != -1) {
            dst.write(data, 0, len);
        }
    }

}
