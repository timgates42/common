package com.tim.lang;

import java.io.*;

public class ExceptionUtils {

    public static String getStackTrace(Throwable err) {
        String message;
        try {
            StringWriter writer = new StringWriter();
            err.printStackTrace(new PrintWriter(writer));
            writer.close();
            message = writer.toString();
        } catch(IOException ioe) {
            message = "";
        }
        return err.getMessage() + "\n" + message;
    }

}
