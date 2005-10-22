package com.tim.app;

import java.io.*;

public class AppUtil {
    public static File getBaseFile(String appname) {
        if(File.separatorChar == '/') {
            return new File(System.getProperty("HOME"), appname);
        }
        return new File("C:\\data\\installs", appname);
    }
}
