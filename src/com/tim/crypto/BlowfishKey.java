package com.tim.crypto;

import java.io.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * This program generates a Blowfish key, retrieves its raw bytes, and 
 * then reinstantiates a Blowfish key from the key bytes.
 * The reinstantiated key is used to initialize a Blowfish cipher for
 * encryption.
 */

public class BlowfishKey {

    public static void main(String[] args) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(args[0].getBytes(), "Blowfish");
        Cipher cipher = Cipher.getInstance("Blowfish");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        FileOutputStream fileout = new FileOutputStream(args[1]);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new CipherOutputStream(fileout, cipher)));
        out.println("This is just an example");
        out.close();
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        FileInputStream filein = new FileInputStream(args[1]);
        BufferedReader in = new BufferedReader(new InputStreamReader(new CipherInputStream(filein, cipher)));
        System.out.println(in.readLine());
        in.close();
    }
}

