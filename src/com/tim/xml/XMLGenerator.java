package com.tim.xml;

import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class XMLGenerator extends DefaultHandler implements XMLReader, Runnable {
 
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger("XMLGenerator");
    static {
        log.setLevel(java.util.logging.Level.ALL);
    }

    private FileWriter xml_output;
    private ContentHandler handler;
    private Object thread_lock = new Object();
    private boolean done = false;
    private TransformerException exception;
    
    public XMLGenerator(File xml_output) throws IOException {
        this.xml_output = new FileWriter(xml_output);
    }

    public void startElement(String name, Hashtable attributes) throws SAXException {
        startElement("", name, name, attributes);
    }

    public void startElement(String uri, String localname, String qname, Hashtable attributes) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        Enumeration e = attributes.keys();
        while(e.hasMoreElements()) {
            String key = (String) e.nextElement(); 
            String value = (String) attributes.get(key);
            attr.addAttribute("", key, key, "CDATA", value);
        }
        handler.startElement(uri, localname, qname, attr);
    }

    public void endElement(String name) throws SAXException {
        endElement("", name, name);
    }

    public void endElement(String uri, String localname, String qname) throws SAXException {
        handler.endElement(uri, localname, qname);
    }

    public void characters(String data) throws SAXException {
        int len = data.length();
        char[] cdata = new char[len];
        data.getChars(0, len, cdata, 0);
        handler.characters(cdata, 0, len);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        handler.characters(ch, start, length);
    }


    public void startDocument() throws SAXException {
        synchronized(thread_lock){
            (new Thread(this)).start();
            try {
                thread_lock.wait();
            } catch(InterruptedException ie){
            }
            checkException();
        }
    }

    public void endDocument() {
        synchronized(thread_lock){
            done = true;
            thread_lock.notify();
        }
    }

    public void checkException() throws SAXException {
        if (exception != null) {
            throw new SAXException(exception.getMessage());
        }
    }

    public void run() {
        synchronized(thread_lock){
            try {
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                SAXSource source = new SAXSource(this, new InputSource(""));
                StreamResult result = new StreamResult(xml_output);
                transformer.transform(source, result);
                xml_output.close();
	    } catch (IOException ioexc) {
                log.throwing("XMLGenerator", "run", ioexc);
                this.exception = new TransformerException(ioexc.toString());
	    } catch (TransformerException exc) {
                log.throwing("XMLGenerator", "run", exc);
                this.exception = exc;
            } finally {
                thread_lock.notify();
	    }
        }
    }
    
    public void parse(InputSource input) throws SAXException {
        handler.startDocument();      
        synchronized(thread_lock) {
            thread_lock.notify();
            try {
                while(!done) {
                    thread_lock.wait();
                    thread_lock.notify();
                }
            } catch(InterruptedException ie){
            }
        }
        handler.endDocument();      
    }
    
    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    } 

    public ContentHandler getContentHandler() { 
        return this.handler;
    } 

    public void setErrorHandler(ErrorHandler handler) {}
    public ErrorHandler getErrorHandler() { return null; }
    public void parse(String systemId) throws SAXException { }
    public DTDHandler getDTDHandler() { return null; }
    public EntityResolver getEntityResolver() { return null; }
    public void setEntityResolver(EntityResolver resolver) {}
    public void setDTDHandler(DTDHandler handler) {}
    public Object getProperty(java.lang.String name) { return null; }
    public void setProperty(java.lang.String name, java.lang.Object value) {} 
    public void setFeature(java.lang.String name, boolean value) {}
    public boolean getFeature(java.lang.String name) { return false; }  

    public static void main ( String[] args ) throws Exception {
        if ( args.length == 0 ) {
            System.err.println( "usage: XMLGenerator <file>" );
            System.exit(1);
        }
        XMLGenerator generate = new XMLGenerator(new File(args[0]));
        generate.startDocument();
        generate.startElement("primary", new Hashtable());
        generate.characters("\n");
        for(int i = 0; i < 10000; i++) {
            generate.characters("    ");
            generate.startElement("secondary", new Hashtable());
            generate.endElement("secondary");
            generate.characters("\n");
        }
        generate.endElement("primary");
        generate.characters("\n");
        generate.endDocument();
    }
    


}

