package com.tim.io;

import java.io.*;
import java.util.logging.*;

public class MasterVFS {

    public static VFS MASTER;

    public static synchronized VFS getFileSystem() {
        if(MASTER == null) {
            MASTER = new MemoryVFS();
            File[] drives = File.listRoots();
            if(drives.length == 1 && drives[0].toString().charAt(0) == File.separatorChar) {
                try {
                    MASTER.mount("drive", new FlatFileVFS(drives[0]));
                } catch(IOException ioe) {
                    Logger.getLogger("VFS").throwing(MasterVFS.class.toString(), "getFileSystem", ioe);
                }
            } else {
                MASTER.mkdir("drives");
                for(int i = 0; i < drives.length; i++) {
                    String name = drives[i].toString();
                    int index = name.indexOf(':');
                    if(index != -1) {
                        name = name.substring(0, index).toLowerCase();
                    }
                    try {
                        MASTER.mount("drives/" + name, new FlatFileVFS(drives[i]));
                    } catch(IOException ioe) {
                        Logger.getLogger("VFS").throwing(MasterVFS.class.toString(), "getFileSystem", ioe);
                    }
                }
            }
        }
        return MASTER;
    }

}
