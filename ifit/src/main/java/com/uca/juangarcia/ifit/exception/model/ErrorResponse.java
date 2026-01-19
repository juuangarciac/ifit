package com.uca.juangarcia.ifit.exception.model;

public class ErrorResponse {
    
    private String error;
    private int status;
    private String timestamp;
    private String message;

    public ErrorResponse(String error, int status, String timestamp, String message) {
        this.error = error;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }  
}
