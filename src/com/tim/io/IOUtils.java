package com.tim.io;

import java.io.*;

public class IOUtils {

    public static String read(InputStream in) throws IOException {
        StringWriter writer = new StringWriter();
        copy(in, writer);
        String result = writer.toString();
        writer.close();
        return result;
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] data = new byte[4096];
        int len;
        while((len = in.read(data)) >= 0) {
            out.write(data, 0, len);
        }
        out.close();
        in.close();
    }

    public static void copy(Reader in, OutputStream out) throws IOException {
        copy(in, new OutputStreamWriter(out));
    }

    public static void copy(InputStream in, Writer out) throws IOException {
        copy(new InputStreamReader(in), out);
    }

    public static void copy(Reader in, Writer out) throws IOException {
        char[] data = new char[4096];
        int len;
        while((len = in.read(data)) >= 0) {
            out.write(data, 0, len);
        }
        out.close();
        in.close();
    }
    
    public static void move(File src, File dst) throws IOException {
        if(!src.exists()) {
            throw new IOException("No source file " + src + " to move.");
        }
        if(dst.exists()) {
            if(!dst.delete()) {
                throw new IOException("Unable to remove destination file " + dst + " before move.");
            }
        }
        boolean result = src.renameTo(dst);
        if(!result) {
            copy(new FileReader(src), new FileWriter(dst));
            if(!src.delete()) {
                dst.delete();
                throw new IOException("Aborting move: Unable to delete src " + src + " after copy.");
            }
        }
    }

}
