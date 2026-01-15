package com.uca.juangarcia.ifit.exception;

public class EmailNotFoundException extends Exception{
    private String email;

    public EmailNotFoundException(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return "User with email " + email + " does not exist.";
    }
}
