package com.tim.io;

import java.io.*;

public abstract class PagedInputStream extends InputStream {
    
    protected byte[] buffer;
    protected int bufferlen;
    protected int bufferoffset;

    public int read() throws IOException {
        byte[] data = new byte[1];
        int len = read(data, 0, 1);
        if(len != 1) {
            return -1;
        }
        return (data[0]&0xFF);
    }
    
    public int read(byte[] data, int offset, int len) throws IOException {
        int read = 0;
        while(true) {
            if(buffer == null || bufferoffset == bufferlen) {
                if(!readBuffer()) {
                    if(read == 0) {
                        return -1;
                    }
                    return read;
                }
            }
            if(bufferlen - bufferoffset >= len) {
                System.arraycopy(buffer, bufferoffset, data, offset, len);
                bufferoffset += len;
                read += len;
                return read;
            } else {
                int available = bufferlen - bufferoffset;
                System.arraycopy(buffer, bufferoffset, data, offset, available);
                read += available;
                offset += available;
                len -= available;
                buffer = null;
                bufferlen = 0;
                bufferoffset = 0;
            }
        }
    }
    
    public void close() throws IOException {
        buffer = null;
    }
    
    protected abstract boolean readBuffer() throws IOException;
    
}
