package com.tim.io;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import org.w3c.dom.Element;

public class FlatFileVFSManager extends AuthenticatedVFSManager {

    public FlatFileVFSManager(InputStream authentication_config) throws IOException {
        super(authentication_config);
    }
    
    public VFS newSession(String username, String password, Element authentication_context) {
        return new FlatFileVFS(new File(authentication_context.getAttribute("home")));
    }

}
