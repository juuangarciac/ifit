package com.uca.juangarcia.ifit.exception.dto;

public class EmailAlreadyExistsException extends Exception {

    private String email;

    public EmailAlreadyExistsException(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return "User with email " + email + " already exists.";
    }
}
