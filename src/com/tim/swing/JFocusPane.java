package com.tim.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class JFocusPane extends FocusAdapter {

    private Component initial_focus;

    public JFocusPane(Component initial_focus) {
        this.initial_focus = initial_focus;
    }

    public static int showDialog(Frame frame, Component panel, String title, Component initial_focus) {
        return showDialog(frame, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, initial_focus);
    }
    
    public static int showDialog(Frame frame, Component panel, String title, int option_type, int message_type, Component initial_focus) {
        JOptionPane pane = new JOptionPane(panel, message_type, option_type, null, null, null);
        pane.addFocusListener(new JFocusPane(initial_focus));
        JDialog dialog = pane.createDialog(frame, title);
        dialog.show();
        dialog.dispose();
        Object val = pane.getValue();
        if(val == null || !(val instanceof Integer)) {
            return JOptionPane.CLOSED_OPTION;
        }
        return ((Integer) val).intValue();
    }
    
    public void focusGained(FocusEvent fe) {
        initial_focus.requestFocus();
    }
    
}

        
