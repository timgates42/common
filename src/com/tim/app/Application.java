package com.tim.app;

import org.w3c.dom.*;
import java.io.*;
import java.net.*;
import com.tim.io.*;
import com.tim.xml.*;
import com.tim.net.handler.*;

public class Application {
    
    public static void init() {
        VFS master = MasterVFS.getFileSystem();
        DefaultHandlerFactory.addHandler("vfs", new VFSHandlerFactory(master));
        ClassLoader loader = Application.class.getClassLoader();
        InputStream data = loader.getResourceAsStream("applications.xml");
        try {
            Document document = XMLUtil.newDocument(data);
            Element main = XMLUtil.getElement(document, "applications");
            Element[] applications = XMLUtil.getElements(main, "application");
            for(int i = 0; i < applications.length; i++) {
                try {
                    String handler = XMLUtil.getString(applications[i], "handler", "");
                    Class classobj = loader.loadClass(handler);
                    ApplicationHandler apphandler = (ApplicationHandler) classobj.newInstance();
                    ApplicationRegistry.registerHandler(XMLUtil.getStringArray(applications[i], "ext", "value", ""), apphandler);
                } catch(ClassNotFoundException cnfe) {
                } catch(InstantiationException ie) {
                } catch(IllegalAccessException iae) {
                }
            }
        } catch(IOException ioe) {
        }
    }
    
    public static void main(String[] args) throws Exception {
        Application.init();
        for(int i = 0; i < args.length; i++) {
            URL url = new URL("vfs:///drives" + args[i]);
            System.out.println(IOUtils.read(url.openStream()));
        }
    }
    
}
