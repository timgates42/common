package com.tim.swing;

import java.util.Collections;
import java.util.Vector;
import javax.swing.JComboBox;

public class JSortedComboBox {

    private JComboBox combobox;
    private Vector list;
    
    public JSortedComboBox() {
        list = new Vector();
        combobox = new JComboBox();
    }
    
    public JComboBox getJComboBox() {
        return combobox;
    }
    
    public void addItem(Object item) {
        int index = Collections.binarySearch(list, item, null);
        if(index < 0) {
            index = -1 - index; 
        }
        list.insertElementAt(item, index);
        combobox.insertItemAt(item, index);
    }
    
    public void removeItem(Object item) {
        combobox.removeItem(item);
        list.remove(item);
    }
    
    public Object getSelectedItem() {
        return combobox.getSelectedItem();
    }
    
}

