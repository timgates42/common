package com.tim.net;

public class HTMLUtil {

    public static final String toHTML(String data) {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            switch(c) {
            case ' ':
                buffer.append("&nbsp;");
                break;
            case '\n':
                buffer.append("<br>");
                break;
            case '\"':
                buffer.append("&quot;");
                break;
            case '&':
                buffer.append("&amp;");
                break;
            case '<':
                buffer.append("&lt;");
                break;
            case '>':
                buffer.append("&gt;");
                break;
            default:
                buffer.append(c);
                break;
            }
        }
        return buffer.toString();
    }

}
