package com.tim.io;

import java.io.*;
import java.net.*;

public interface VFSRandomAccessFile {

    void seek(long position) throws IOException;
    void skip(int len) throws IOException;
    int read(byte[] data, int offset, int len) throws IOException;
    void write(byte[] data, int offset, int len) throws IOException;
    void setLength(long len) throws IOException;
    long tell() throws IOException;
    void close() throws IOException;
    long length() throws IOException;

}
