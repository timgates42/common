package com.tim.lang;

import java.util.Vector;

public class StringUtils {

    public static String[] split(String src, String separator) {
        return split(src, separator, false);
    }
    
    public static String[] split(String src, String separator, boolean ignore_empty) {
        Vector v = new Vector();
        int i = 0;
        while( i < src.length()) {
            int next_index = src.indexOf(separator, i);
            if(next_index == -1) {
                next_index = src.length();
            }
            if(i != next_index || !ignore_empty) {
                v.addElement(src.substring(i, next_index));
            }
            i = next_index + separator.length();
        }
        String[] result = new String[v.size()];
        v.toArray(result);
        return result;
    }
    
    public static String zpad(int value, int length) {
        return zpad(value, length, true);
    }

    public static String zpad(long value, int length) {
        return zpad(value, length, true);
    }

    public static String zpad(int value, int length, boolean leftpad) {
        String v = Integer.toString(value);
        return zpad(v, length, leftpad);
    }
    
    public static String zpad(long value, int length, boolean leftpad) {
        String v = Long.toString(value);
        return zpad(v, length, leftpad);
    }
    
    public static String zpad(String v, int length, boolean leftpad) {
        if(v.length() >= length) {
            return v;
        }
        String pad = repeat('0', length - v.length());
        if(leftpad) {
            return pad + v;
        } else {
            return v + pad;
        }
    }
    
    public static String repeat(char c, int count) {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < count; i++) {
            buffer.append(c);
        }
        return buffer.toString();
    }

}
