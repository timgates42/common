package com.tim.xml;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.*;

public class XMLUtil {
    
    private static XMLUtil INSTANCE; 
    private DocumentBuilder builder;
    private TransformerFactory transformer_factory;
    
    private XMLUtil() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            transformer_factory = TransformerFactory.newInstance();
        } catch(ParserConfigurationException pce) {
            throw new RuntimeException(pce.getMessage());
        }
    }
    
    private static final synchronized XMLUtil getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new XMLUtil();
        }
        return INSTANCE;
    }
    
    public DocumentBuilder getBuilder() {
        return builder;
    }
    
    public static Document newDocument() {
        DocumentBuilder builder = getInstance().getBuilder();
        synchronized(builder) {
            return builder.newDocument();
        }
    }
    
    public static Document newDocument(InputStream in) throws IOException {
        try {
            DocumentBuilder builder = getInstance().getBuilder();
            synchronized(builder) {
                return builder.parse(in);
            }
        } catch(SAXException sax) {
            throw new IOException(sax.getMessage());
        }
    }
    
    public TransformerFactory getTransformerFactory() {
        return transformer_factory;
    }
    
    public static Transformer newTransformer() {
        try {
            TransformerFactory factory = getInstance().getTransformerFactory();
            synchronized(factory) {
                return factory.newTransformer();
            }
        } catch(TransformerConfigurationException tce) {
            throw new RuntimeException(tce.getMessage());
        }
    }

    public static void save(Document document, OutputStream out) throws IOException {
        try {
            Transformer transformer = newTransformer();
            transformer.transform(new DOMSource(document), new StreamResult(out));
            out.close();
        } catch(TransformerException te) {
            System.out.println(document + " " + out);
            te.printStackTrace();
            throw new IOException(te.toString());
        }
    }
    
    public static Element appendChild(Document document, Node element, String name) {
        Element child = document.createElement(name);
        element.appendChild(child);
        return child;
    }
    
    public static Element getElement(Node node, String name) {
        try {
            return getElementCommon(node, name, false);
        } catch(ElementNotFoundException enfe) {
            return null;
        }
    }
    
    public static Element getElementE(Node node, String name) throws ElementNotFoundException {
            return getElementCommon(node, name, true);
    }
    
    public static Element getElementCommon(Node node, String name, boolean except) throws ElementNotFoundException {
        if(node == null || name == null) {
            if(except) {
                throw new ElementNotFoundException();
            } else {
                return null;
            }
        }
        NodeList nodes = node.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++) {
            Node inspect = nodes.item(i);
            if(inspect instanceof Element && ((Element) inspect).getTagName().equals(name)) {
                return (Element) inspect;
            }
        }
        if(except) {
            throw new ElementNotFoundException();
        } else {
            return null;
        }
    }

    public static Element[] getElements(Node node, String name) {
        if(node == null || name == null) {
            return new Element[0];
        }
        NodeList nodes = node.getChildNodes();
        Vector list = new Vector();
        for(int i = 0; i < nodes.getLength(); i++) {
            Node inspect = nodes.item(i);
            if(inspect instanceof Element && ((Element) inspect).getTagName().equals(name)) {
                list.addElement(inspect);
            }
        }
        Element[] result = new Element[list.size()];
        list.toArray(result);
        return result;
    }
    
    public static void verifyAttribute(Element elem, String name) throws ElementNotFoundException {
        if(!elem.hasAttribute(name)) {
            throw new ElementNotFoundException();
        }
    }
    
    public static boolean getBoolean(Element elem, String name, boolean deflt) {
        String attr = elem.getAttribute(name);
        if(attr == null || attr.length() == 0) {
            return deflt;
        }
        return attr.equalsIgnoreCase("true");
    }

    public static int getInt(Element elem, String name, int deflt) {
        try {
            return Integer.parseInt(getStringE(elem, name));
        } catch(ElementNotFoundException enfe) {
            return deflt;
        } catch(NumberFormatException nfe) {
            return deflt;
        }
    }
    
    public static int getIntE(Element elem, String name) throws ElementNotFoundException {
        return Integer.parseInt(getStringE(elem, name));
    }
    
    public static void setInt(Element elem, String name, int value) {
        setString(elem, name, Integer.toString(value));
    }
    
    public static long getLong(Element elem, String name, long deflt) {
        try {
            return Long.parseLong(getStringE(elem, name));
        } catch(ElementNotFoundException enfe) {
            return deflt;
        } catch(NumberFormatException nfe) {
            return deflt;
        }
    }
    
    public static long getLongE(Element elem, String name) throws ElementNotFoundException {
        return Long.parseLong(getStringE(elem, name));
    }
    
    public static void setLong(Element elem, String name, long value) {
        setString(elem, name, Long.toString(value));
    }
    
    public static float getFloat(Element elem, String name, float deflt) {
        try {
            return Float.parseFloat(getStringE(elem, name));
        } catch(ElementNotFoundException enfe) {
            return deflt;
        } catch(NumberFormatException nfe) {
            return deflt;
        }
    }
    
    public static float getFloatE(Element elem, String name) throws ElementNotFoundException {
        return Float.parseFloat(getStringE(elem, name));
    }
    
    public static void setFloat(Element elem, String name, float value) {
        setString(elem, name, Float.toString(value));
    }
    
    public static String getString(Element elem, String name, String deflt) {
        String attr = elem.getAttribute(name);
        if(attr == null || attr.length() == 0) {
            return deflt;
        }
        return attr;
    }
    
    public static String getStringE(Element elem, String name) throws ElementNotFoundException {
        verifyAttribute(elem, name);
        return elem.getAttribute(name);
    }
    
    public static void setString(Element elem, String name, String value) {
        elem.setAttribute(name, value);
    }
    
    public static String[] getStringArray(Element elem, String name, String attr, String dflt) {
        Element[] elements = getElements(elem, name);
        String[] data = new String[elements.length];
        for(int i = 0; i < data.length; i++) {
            data[i] = getString(elements[i], attr, dflt);
        }
        return data;
    }

}
