package com.tim.io;

import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import com.tim.xml.XMLUtil;
import com.tim.crypto.MD5;
import com.tim.net.Base64;

public class AuthenticationEngine {

    private Hashtable authentication;

    public AuthenticationEngine(InputStream config) throws IOException {
        authentication = new Hashtable();
        init(XMLUtil.newDocument(config));
    }
    
    private void init(Document document) {
        Element users = XMLUtil.getElement(document, "users");
        Element[] elements = XMLUtil.getElements(users, "user");
        for(int i = 0; i < elements.length; i++) {
            addUser(elements[i].getAttribute("username"), elements[i].getAttribute("password"), elements[i]);
        }
    }

    private void addUser(String username, String password, Element context) {
        System.out.println(username + " - " + password);
        authentication.put(username, new UserConfig(username, password, context));
    }

    /**
     * Returns the authentication context if authentication suceeds.
     */
    public Element authenticateUser(String username, String password) throws AuthenticationException {
        UserConfig config = retrieveUserConfig(username);
        if(!config.validatePassword(password)) {
            throw new AuthenticationException();
        }
        return config.getContext();
    }
    
    public Element retrieveUserContext(String username) throws AuthenticationException {
        UserConfig config = retrieveUserConfig(username);
        return config.getContext();
    }
    
    public UserConfig retrieveUserConfig(String username) throws AuthenticationException {
        if(!authentication.containsKey(username)) {
            throw new AuthenticationException();
        }
        return (UserConfig) authentication.get(username);
    }
    
    private static final class UserConfig {
    
        private String username;
        private String password;
    


    private Element context;
        
        public UserConfig(String username, String password, Element context) {
            this.username = username;
            this.password = password;
            this.context = context;
        }
        
        public boolean validatePassword(String check) {
            String hashed = Base64.encodeBytes((new MD5(check)).Final());
            return hashed.equals(password);
        }
        
        public Element getContext() {
            return context;
        }
        
    }
    
    public static void main(String[] args) throws Exception {
        String username = args[0];
        String password = args[1];
        String home = args[2];
        Document document = XMLUtil.newDocument();
        Element users = document.createElement("users");
        document.appendChild(users);
        Element user = document.createElement("user");
        users.appendChild(user);
        user.setAttribute("username", username);
        user.setAttribute("password", Base64.encodeBytes((new MD5(password)).Final()));
        user.setAttribute("home", home);
        XMLUtil.save(document, System.out);
    }

}
