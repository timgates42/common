package com.tim.adt;

import java.util.*;

public class Heap {

    public Vector elements;
    
    public Heap() {
        elements = new Vector();
    }
    
    /**
     * Returns the index of the left child
     */
    private int left(int i) {
        return ((i + 1) << 1) - 1;
    }
    
    /**
     * Returns the index of the right child
     */
    private int right(int i) {
        return ((i + 1) << 1);
    }

    /**
     * Returns the index of the parent
     */
    private int parent(int i) {
        return ((i + 1) >> 1) - 1;
    }
    
    /**
     * Exchanges the elements stored at the two locations
     */
    private void exchange(int i, int j) {
        Object temp = elements.elementAt(j);
        elements.setElementAt(elements.elementAt(i), j);
        elements.setElementAt(temp, i);
    }

    /**
     * Also known as downheap, restores the heap condition
     * starting at node i and working its way down.
     */
    private void heapify(int i) {
        int l = left(i);
        int r = right(i);
        int smallest;
        
        if(l < elements.size() && ((HeapElement)elements.elementAt(l)).lessThan(elements.elementAt(i))) {
            smallest = l;
        } else {
            smallest = i;
        }
        
        if(r < elements.size() && ((HeapElement)elements.elementAt(r)).lessThan(elements.elementAt(smallest))) {
            smallest = r;
        }
        
        if(smallest != i) {
            exchange(i, smallest);
            heapify(smallest);
        }
    }

    /**
     * Retrieves the minimum element.
     */
    public HeapElement peek() {
        if (elements.size() == 0)
            return null;
        
        return (HeapElement) elements.elementAt(0);
    }
    
    /**
     * Removes the minimum (top) element from the Heap, decreases the
     * size of the heap by one, and returns the minimum element.
     */
    public HeapElement pop() {
        if (elements.size() == 0)
            return null;
        
        Object min = elements.elementAt(0);
        
        // move the last key to the top, decrease size, and downheap
        elements.setElementAt(elements.lastElement(), 0);
        elements.setSize(elements.size() - 1);
        heapify(0);
        
        return (HeapElement) min;
    }

    /**
     * Inserts key into the heap, and then upheaps that key to a
     * position where the heap property is satisfied.
     */
    public void insert(HeapElement key) {
        int i = elements.size();
        elements.setSize(i + 1);
        
        // upheap if necessary
        while (i > 0 && ((HeapElement)elements.elementAt(parent(i))).greaterThan(key)) {
            elements.setElementAt(elements.elementAt(parent(i)), i);
            i = parent(i);
        }
        
        elements.setElementAt(key, i);
    }
    
    public int size() {
        return elements.size();
    }

}

