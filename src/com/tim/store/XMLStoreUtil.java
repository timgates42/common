package com.tim.store;

import static com.tim.store.Store.*;
import com.tim.xml.XMLUtil;
import com.tim.xml.ElementNotFoundException;

import org.w3c.dom.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

public class XMLStoreUtil {

    public static final String XML_TAG_DOCUMENT = "document";
    public static final String XML_TAG_STORE = "store";

    public static void saveXMLStore(Storable store, OutputStream out) throws IOException {
        Document document = XMLUtil.newDocument();
        Element nodeset = document.createElement(XML_TAG_DOCUMENT);
        document.appendChild(nodeset);
        Element node = document.createElement(XML_TAG_STORE);
        nodeset.appendChild(node);
        Store xmlstore = new XMLNodeStore(document, node, StoreMode.Writing);
        store.store(xmlstore);
        XMLUtil.save(document, out);
    }
    
    public static void loadXMLStore(Storable store, InputStream in) throws IOException {
        try {
            Document document = XMLUtil.newDocument(in);
            Element nodeset = XMLUtil.getElementE(document, XML_TAG_DOCUMENT);
            Element node = XMLUtil.getElementE(nodeset, XML_TAG_STORE);
            Store xmlstore = new XMLNodeStore(document, node, StoreMode.Reading);
            store.store(xmlstore);
        } catch (ElementNotFoundException e) {
            throw new IOException(e.toString());
        }
    }
    
    private static final class TestStore implements Storable {
        public void store(Store store) {
            store.storeInt("test", 5, 0);
            store.storeString("test2", "value", "default");
        }
    }
    
    public static void main(String[] args) throws IOException {
        saveXMLStore(new TestStore(), System.out);
        System.out.flush();
    }

}

