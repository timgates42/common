/*
 * User: Timothy Gates
 * Date: Oct 14, 2001
 * Time: 1:05:25 AM
 *
 * Copyright 1995 - 2001
 * Timothy Dyson Gates
 * All Rights Reserved
 */
package com.tim.adt;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Arrays;

public class TreeNode {

    private Hashtable nodes;
    private Object value;

    public TreeNode( Object value ) {
        this.nodes = new Hashtable();
        this.value = value;
    }

    public String[] listKeys() {
        Vector list = new Vector();
        Enumeration enumeration = nodes.keys();
        while( enumeration.hasMoreElements() ) {
            list.addElement( enumeration.nextElement() );
        }
        String[] list_strings = new String[list.size()];
        list.toArray( list_strings );
        Arrays.sort( list_strings );
        return list_strings;
    }

    public boolean containsNode( String key ) {
        return nodes.containsKey( key );
    }

    public boolean removeNode( String key ) {
        Object obj = nodes.remove( key );
        return obj != null;
    }

    public TreeNode retrieveNode( String key ) throws ElementNotFoundException {
        if( !nodes.containsKey( key ) ) {
            throw new ElementNotFoundException( "TreeNode does not contain key " + key );
        }
        return (TreeNode) nodes.get( key );
    }

    public void addElement( String key, TreeNode value ) throws DuplicateElementException {
        if( nodes.containsKey( key ) ) {
            throw new DuplicateElementException( "TreeNode already contains key " + key );
        }
        nodes.put( key, value );
    }

    public Object getValue() {
        return value;
    }

    public void setValue( Object value ) {
        this.value = value;
    }

}
