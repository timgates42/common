package com.tim.io;

import java.io.*;

public abstract class PagedOutputStream extends OutputStream {

    protected int bufferlen;
    protected byte[] buffer;
    protected int pagesize;
    
    public PagedOutputStream(int pagesize) {
        this.pagesize = pagesize;
        this.buffer = new byte[pagesize];
    }

    public void write(int data) throws IOException {
        write(new byte[] {(byte) data}, 0, 1);
    }
    
    public void write(byte[] data, int offset, int len) throws IOException {
        try {
            while(true) {
                if(bufferlen + len >= pagesize) {
                    int required = pagesize - bufferlen;
                    System.arraycopy(data, offset, buffer, bufferlen, required);
                    bufferlen = pagesize;
                    offset += required;
                    len -= required;
                    pushBuffer();
                    bufferlen = 0;
                } else {
                    System.arraycopy(data, offset, buffer, bufferlen, len);
                    bufferlen += len;
                    return;
                }
            }
        } catch(RuntimeException re) {
            re.printStackTrace();
            throw re;
        }
    }
    
    protected abstract void pushBuffer() throws IOException;
    
    public void close() throws IOException {
        pushBuffer();
    }
    
}
