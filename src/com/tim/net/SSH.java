package com.tim.net;

import net.lag.jaramiko.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class SSH {

    private static final String PRIVATE_HOST_KEY_FILENAME = "host_key.priv";
    private static final String PUBLIC_HOST_KEY_FILENAME = "host_key.pub";
    private static final String PRIVATE_AUTH_KEY_FILENAME = "auth_key.priv";
    private static final String PUBLIC_AUTH_KEY_FILENAME = "auth_key.pub";
    
    private static final int TIMEOUT = 15000;

    public static final Channel openClientEncryption(Socket connection, File basedir, String appname) throws IOException {
        return openClientEncryption(connection, basedir, null, null, appname, null);
    }
    
    public static final Channel openClientEncryption(Socket connection, File basedir, File public_host_key, File private_auth_key, String appname, String username) throws IOException {
        if(username == null) {
            username = appname;
        }
        ClientTransport t = openClientEncryptionTransport(connection, basedir, public_host_key, private_auth_key, username);
        Channel chan = t.openChannel(appname, null, TIMEOUT);
        if(chan == null) {
            throw new SSHException("Failed to open channel on connection.");
        }
        return chan;
    }
    
    public static final ClientTransport openClientEncryptionTransport(Socket connection, File basedir, File public_host_key, File private_auth_key, String username) throws IOException {
        if(basedir != null) {
            public_host_key = new File(basedir, PUBLIC_HOST_KEY_FILENAME);
            private_auth_key = new File(basedir, PRIVATE_AUTH_KEY_FILENAME);
        }
        PKey host_key = openPublicKeyFile(public_host_key);
        PKey auth_key = PKey.readPrivateKeyFromStream(new FileInputStream(private_auth_key), null);
        ClientTransport t = new ClientTransport(connection);
        t.start(host_key, TIMEOUT);
        String[] secondary_authentication = t.authPrivateKey(username, auth_key, TIMEOUT);
        if(secondary_authentication != null && secondary_authentication.length > 0) {
            throw new SSHException("Secondary authentication required.");
        }
        /*if(!t.getRemoteServerKey().equals(host_key)) {
            throw new SSHException("Unexpected host key, possible man in the middle attack.");
        }*/
        if(!t.isAuthenticated()) {
            throw new SSHException("Authentication Failed.");
        }
        return t;
    }

    public static final PKey openPublicKeyFile(File filename) throws IOException {
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        return PKey.createFromBase64(buffer.toString());
    }

    public static final void writePublicKeyFile(File filename, PKey key) throws IOException {
        PrintWriter printer = new PrintWriter(new FileWriter(filename));
        printer.println(key.getBase64());
        printer.close();
    }

    private static final class SSHServerSettings implements ServerInterface {
    
        private PKey key;
        private String appname;
        
        public SSHServerSettings(PKey key, String appname) {
            this.key = key;
            this.appname = appname;
        }

        public int checkChannelRequest(String kind, int chanID) {
            if(kind == this.appname) {
                return ChannelError.SUCCESS;
            }
            return ChannelError.ADMINISTRATIVELY_PROHIBITED;
        }
        
        public String getAllowedAuths(String username) {
            return "publickey";
        }
    
        public int checkAuthNone(String username) {
            return AuthError.FAILED;
        }
        
        public int checkAuthPassword(String username, String password) {
            return AuthError.FAILED;
        }
        
        public int checkAuthPublicKey(String username, PKey key) {
            if(username.equals(this.appname) && key.equals(this.key)) {
                return AuthError.SUCCESS;
            }
            return AuthError.FAILED;
        }
        
        public List checkGlobalRequest(String kind, Message m) {
            return null;
        }
    
        public boolean checkChannelPTYRequest(Channel c, String term, int width, int height, int pixelWidth, int pixelHeight, String modes) {
            return false;
        }
        
        public boolean checkChannelShellRequest(Channel c) {
            return false;
        }
        
        public boolean checkChannelExecRequest(Channel c, String command) {
            return false;
        }
        
        public boolean checkChannelSubsystemRequest(Channel c, String name) {
            return false;
        }
    
        public boolean checkChannelWindowChangeRequest(Channel c, int width, int height, int pixelWidth, int pixelHeight) {
            return false;
        }
    }
    
    public Channel openServerEncryption(Socket connection, File basedir, File private_host_key, File public_auth_key, String appname) throws IOException {
        if(basedir != null) {
            private_host_key = new File(basedir, PRIVATE_HOST_KEY_FILENAME);
            public_auth_key = new File(basedir, PUBLIC_AUTH_KEY_FILENAME);
        }
        PKey host_key = PKey.readPrivateKeyFromStream(new FileInputStream(private_host_key), null);
        PKey auth_key = openPublicKeyFile(public_auth_key);
        ServerTransport t = new ServerTransport(connection);
        t.addServerKey(host_key);
        SSHServerSettings server = new SSHServerSettings(auth_key, appname);
        t.start(server, TIMEOUT);
        if(!t.isAuthenticated()) {
            throw new SSHException("Authentication Failed.");
        }
        Channel chan = t.accept(TIMEOUT);
        if(chan == null) {
            throw new SSHException("No channel requested before timeout.");
        }
        return chan;
    }

    /*
    
     * Writing a private key file not yet supported in jaramiko
    
    private static final String[][] KEYS = new String[][] {{PRIVATE_HOST_KEY_FILENAME, PUBLIC_HOST_KEY_FILENAME}, {PRIVATE_AUTH_KEY_FILENAME, PUBLIC_AUTH_KEY_FILENAME}};
    
    public void makeKeys(File basedir) {
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
        for(int i = 0; i < KEYS.length; i++) {
            String privname = KEYS[i][0];
            String pubname = KEYS[i][1];
            File priv = new File(basedir, privname);
            File pub = new File(basedir, pubname);
            PKey key = RSAKey.generate(bits=1024, rand);
            if(!priv.exists()) {
                key.write_private_key_file(priv, None)
        if not os.path.exists(pub):
            writePublicKeyFile(pub, key)

    */

}
