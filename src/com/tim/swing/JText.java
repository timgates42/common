/*
 * Created by IntelliJ IDEA.
 * User: Timothy Gates
 * Date: Sep 2, 2001
 * Time: 12:36:12 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.tim.swing;

import javax.swing.*;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyListener;


public class JText extends JTextPane implements FocusListener, KeyListener {

    private static final int LOST = 0, GAINED = 1, PROCESSED = 2;
    private int focusState;

    public JText() {
        super();
        addFocusListener(this);
        addKeyListener(this);
    }

    public void keyTyped(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t' ) {
            if ( focusState == PROCESSED ) {
                if ( e.isShiftDown() ) {
                    FocusManager.getCurrentManager().focusPreviousComponent(this);
                } else {
                    FocusManager.getCurrentManager().focusNextComponent(this);
                }
            } else {
                focusState = PROCESSED;
                e.consume();
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t' ) {
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
        if ( e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyChar() == '\t' ) {
            e.consume();
        }
    }

    public void focusGained(FocusEvent e) {
        focusState = GAINED;
    }

    public void focusLost(FocusEvent e) {
        focusState = LOST;
    }
}
