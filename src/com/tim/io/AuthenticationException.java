package com.tim.io;

public class AuthenticationException extends Exception {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException() {
        super();
    }
}
