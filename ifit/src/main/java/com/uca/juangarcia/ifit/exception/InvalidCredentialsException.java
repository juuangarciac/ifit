package com.uca.juangarcia.ifit.exception;

public class InvalidCredentialsException extends Exception {
    
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String username, String message) {
        super("Invalid credentials for user: " + username + " - " + message);
    }
    
}
