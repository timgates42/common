package com.tim.io;

public class MemoryVFSManager implements VFSManager {

    public VFS newSession(String username, String password) {
        return new MemoryVFS();
    }
    
}
