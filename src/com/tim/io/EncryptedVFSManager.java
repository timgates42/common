package com.tim.io;

import java.io.*;
import org.w3c.dom.Element;

public class EncryptedVFSManager implements VFSManager {

    private AuthenticationEngine authentication;

    public EncryptedVFSManager(InputStream authentication_config) throws IOException {
        authentication = new AuthenticationEngine(authentication_config);
    }
    
    public VFS newSession(String username, String password) throws AuthenticationException {
        Element authentication_context = authentication.retrieveUserContext(username);
        return new EncryptedVFS(new VFile(MasterVFS.getFileSystem(), authentication_context.getAttribute("home")), password);
    }
    
}

