/*
 * Created by IntelliJ IDEA.
 * User: Timothy Gates
 * Date: Oct 17, 2001
 * Time: 11:43:54 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.tim.io;

import com.tim.lang.UnaryAction;

import java.util.Vector;
import java.io.File;

public class ExtensionCollecter implements UnaryAction {

    private Vector results;
    private String extension;

    public ExtensionCollecter( Vector results ) {
        this.results = results;
        this.extension = "";
    }

    public ExtensionCollecter( Vector results, String extension ) {
        this.results = results;
        this.extension = extension;
    }

    public void execute( Object arg ) {
        if ( ! ( arg instanceof File ) ) {
            return;
        }
        File file = (File) arg;
        if ( file.isDirectory() ) {
            return;
        }
        if ( ! file.getName().toLowerCase().endsWith( extension ) ) {
            return;
        }
        results.addElement( file );
    }

}
