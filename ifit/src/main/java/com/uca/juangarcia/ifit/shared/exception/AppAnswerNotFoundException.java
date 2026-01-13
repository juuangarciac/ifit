package com.uca.juangarcia.ifit.shared.exception;

public class AppAnswerNotFoundException extends Exception {

    private Long appAnswerId;
    
    public AppAnswerNotFoundException(Long appAnswerId) {
        this.appAnswerId = appAnswerId;
    }

    public AppAnswerNotFoundException(String message) {
        super(message);
        this.appAnswerId = null;
    }

    public Long getAppAnswerId() {
        return appAnswerId;
    }

    public void setAppAnswerId(Long appAnswerId) {
        this.appAnswerId = appAnswerId;
    }

    @Override
    public String getMessage() {
        return "AppAnswer with ID " + appAnswerId + " not found.";
    }
}
