package com.tim.app;

import java.io.*;
import com.tim.io.*;
import java.net.*;

public interface ApplicationHandler {
    
    void open(VFS vfs, String vfspath, URL url) throws IOException;
    
}
