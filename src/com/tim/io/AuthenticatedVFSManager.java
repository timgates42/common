package com.tim.io;

import java.io.InputStream;
import java.io.IOException;
import org.w3c.dom.Element;

public abstract class AuthenticatedVFSManager implements VFSManager {

    private AuthenticationEngine authentication;

    public AuthenticatedVFSManager(InputStream authentication_config) throws IOException {
        authentication = new AuthenticationEngine(authentication_config);
    }
    
    public VFS newSession(String username, String password) throws AuthenticationException {
        return newSession(username, password, authentication.authenticateUser(username, password));
    }
    
    public abstract VFS newSession(String username, String password, Element authentication_context) throws AuthenticationException;
    
}
