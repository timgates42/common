package com.tim.io;

public interface VFSManager {

    VFS newSession(String username, String password) throws AuthenticationException;
    
}
