/*
 * User: Timothy Gates
 * Date: Oct 14, 2001
 * Time: 1:16:43 AM
 *
 * Copyright 1995 - 2001
 * Timothy Dyson Gates
 * All Rights Reserved
 */
package com.tim.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Properties {

    private static DocumentBuilderFactory document_builder_factory;

    private Hashtable properties;

    public Properties() {
        properties = new Hashtable();
    }

    private static DocumentBuilder getDocumentBuilder() {
        if( document_builder_factory == null ) {
            document_builder_factory = DocumentBuilderFactory.newInstance();
        }
        try {
            return document_builder_factory.newDocumentBuilder();
        } catch( ParserConfigurationException pce ) {
            throw new RuntimeException( pce.toString() );
        }
    }

    public void toStream( OutputStream out ) throws IOException {
        try {
            Document doc = getDocumentBuilder().newDocument();
            TransformerFactory fac = TransformerFactory.newInstance();
            Transformer trans = fac.newTransformer();
            save( doc );
            trans.transform( new DOMSource( doc ), new StreamResult( out ) );
            out.write( '\n' );
        } catch( TransformerException te ) {
            throw new RuntimeException( te.toString() );
        }
    }

    public void fromStream( InputStream in ) throws IOException, SAXException {
        load( null, getDocumentBuilder().parse( in ) );
    }

    private void load( String property, Node node ) {
        int type = node.getNodeType();
        switch( type ) {
        case Node.ELEMENT_NODE:
            property = node.getNodeName();
            break;
        case Node.TEXT_NODE:
            String value = node.getNodeValue();
            if( value != null && value.trim().length() != 0 ) {
                addString( property, value );
            }
            break;
        default:
            property = null;
        }

        for( Node child = node.getFirstChild(); child != null; child = child.getNextSibling() ) {
            load( property, child );
        }
    }

    private void save( Document doc ) {
        Node project = doc.createElement( "project" );
        doc.appendChild( project );
        project.appendChild( doc.createTextNode( "\n" ) );
        for( Enumeration e = properties.keys(); e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            for( Enumeration vals = ( (Vector) properties.get( key ) ).elements(); vals.hasMoreElements(); ) {
                project.appendChild( doc.createTextNode( "   " ) );
                Node data = doc.createElement( key );
                project.appendChild( data );
                Node text = doc.createTextNode( vals.nextElement().toString() );
                data.appendChild( text );
                project.appendChild( doc.createTextNode( "\n" ) );
            }
        }
    }

    public void clear() {
        properties.clear();
    }

    public void setString( String key, String value ) {
        Vector list = (Vector) properties.get( key );
        if( list == null ) {
            list = new Vector();
            properties.put( key, list );
        } else {
            list.clear();
        }
        list.addElement( value );
    }

    public void clear( String key ) {
        Vector list = (Vector) properties.get( key );
        if( list != null ) {
            list.clear();
        }
    }

    public String getString( String key ) {
        Vector list = (Vector) properties.get( key );
        if( list == null || list.size() == 0 ) {
            return null;
        }
        return (String) list.elementAt( 0 );
    }

    public void addString( String key, String value ) {
        Vector list = (Vector) properties.get( key );
        if( list == null ) {
            list = new Vector();
            properties.put( key, list );
        }
        list.add( value );
    }

    public String[] getStrings( String key ) {
        Vector list = (Vector) properties.get( key );
        if( list == null) {
            return null;
        }
        String[] array = new String[list.size()];
        list.toArray( array );
        return array;
    }

}
