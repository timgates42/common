package com.tim.io;

import com.tim.lang.UnaryAction;

import java.io.*;
import java.util.Vector;

public class TraverseDirectory {

    public static Vector collect( File base, String extension ) {
        Vector result = new Vector();
        ExtensionCollecter action = new ExtensionCollecter( result, extension );
        traverse( base, true, false, action );
        return result;
    }

    public static void traverse( File base,
                                 boolean files,
                                 boolean directories,
                                 UnaryAction action ) {
        if( !base.exists() ) {
            return;
        }
        if( !base.isDirectory() ) {
            if( files ) {
                action.execute( base );
            }
            return;
        } else if( directories ) {
            action.execute( base );
        }
        String[] contents = base.list();
        String path = base.getAbsolutePath();
        for( int i = 0; i < contents.length; i++ ) {
            traverse( new File( path + File.separator + contents[i] ),
                      files, directories, action );
        }
    }

}
