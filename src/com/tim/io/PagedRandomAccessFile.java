package com.tim.io;

import java.io.IOException;

public abstract class PagedRandomAccessFile implements VFSRandomAccessFile {
    
    protected int actual_pagesize;

    protected long position;
    protected byte[] buffer;
    protected int bufferoffset;
    protected int usedlen;
    protected boolean page_touched;
    protected boolean page_read;
    protected long pageid;
    
    public PagedRandomAccessFile(int actual_pagesize) {
        this.actual_pagesize = actual_pagesize;
        pageid = -1;
    }
    
    public abstract boolean readPage(long pageid) throws IOException;
    public abstract void commitPage(long pageid) throws IOException;
    public abstract void doSetLength(long position) throws IOException;
    
    public long getPageId(long position) {
        return position / actual_pagesize;
    }
    
    public int getPageOffset(long position) {
        return (int) position % actual_pagesize;
    }
    
    public boolean moveToPage() throws IOException {
        long nextpageid = getPageId(position);
        bufferoffset = getPageOffset(position);
        if(nextpageid == pageid) {
            return false;
        }
        checkCommit();
        pageid = nextpageid;
        page_read = false;
        return true;
    }
    
    public void checkCommit() throws IOException {
        if(page_touched && pageid >= 0) {
            commitPage(pageid);
        }
        page_touched = false;
    }
    
    public boolean moveToPageAndRead() throws IOException {
        if(!moveToPage()) {
            return false;
        }
        if(!page_read) {
            if(!readPage(pageid)) {
                return false;
            }
            page_read = true;
        }
        return true;
    }
    
    public void seek(long position) throws IOException {
        this.position = position;
        moveToPage();
    }
    
    public void skip(int len) throws IOException {
        this.position += position;
        moveToPage();
    }
    
    public int read(byte[] data, int offset, int len) throws IOException {
        if(len == 0) {
            return 0;
        }
        int read = 0;
        while(true) {
            if(buffer == null || bufferoffset == usedlen) {
                if(bufferoffset != actual_pagesize) {
                    return read;
                }
                if(!moveToPageAndRead()) {
                    if(read == 0) {
                        return -1;
                    }
                    return read;
                }
            }
            if(usedlen - bufferoffset >= len) {
                System.arraycopy(buffer, bufferoffset, data, offset, len);
                bufferoffset += len;
                read += len;
                position += len;
                return read;
            } else {
                int available = usedlen - bufferoffset;
                System.arraycopy(buffer, bufferoffset, data, offset, available);
                position += available;
                read += available;
                buffer = null;
                bufferoffset = 0;
                usedlen = 0;
            }
        }
    }
    
    public void write(byte[] data, int offset, int len) throws IOException {
        if(len == 0) {
            return;
        }
        while(true) {
            if(buffer == null || bufferoffset == actual_pagesize) {
                if(!moveToPageAndRead()) {
                    buffer = new byte[actual_pagesize];
                    bufferoffset = 0;
                    usedlen = 0;
                }
            }
            page_touched = true;
            if(bufferoffset + len >= actual_pagesize) {
                int required = actual_pagesize - bufferoffset;
                System.arraycopy(data, offset, buffer, bufferoffset, required);
                offset += required;
                len -= required;
                position += required;
                bufferoffset += required;
                if(bufferoffset > usedlen) {
                    usedlen = bufferoffset;
                }
            } else {
                System.arraycopy(data, offset, buffer, bufferoffset, len);
                bufferoffset += len;
                position +=  len;
                if(bufferoffset > usedlen) {
                    usedlen = bufferoffset;
                }
                return;
            }
        }
    }
    
    public void setLength(long len) throws IOException {
        long last_page = getPageId(len-1);
        if(pageid <= last_page) {
            checkCommit();
        }
        if(position > len) {
            position = len;
        }
        page_touched = false;
        page_read = false;
        doSetLength(len);
        moveToPage();
    }
    
    public long tell() throws IOException {
        return position;
    }
    
    public void close() throws IOException {
        checkCommit();
    }

}

    
    