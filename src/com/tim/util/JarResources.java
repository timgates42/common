package com.tim.util;

import com.tim.lang.StringUtils;
import java.util.zip.*;
import java.io.*;

public class JarResources {
    
    public static InputStream getLibraryResource(String filename) {
        String[] elements = StringUtils.split(System.getProperty("java.class.path"), System.getProperty("path.separator"));
        for(int i = 0; i < elements.length; i++) {
            try {
                ZipFile file = new ZipFile(elements[i]);
                ZipEntry entry = file.getEntry(filename);
                if(entry != null) {
                    InputStream data = file.getInputStream(entry);
                    ByteArrayOutputStream writer = new ByteArrayOutputStream();
                    DataTransfer.copy(data, writer);
                    data.close();
                    file.close();
                    byte[] result = writer.toByteArray();
                    writer.close();
                    return new ByteArrayInputStream(result);
                }
                file.close();
            } catch(ZipException ze) {
            } catch(IOException ioe) {
            }
        }
        return null;
    }
    
}