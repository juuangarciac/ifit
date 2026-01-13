package com.uca.juangarcia.ifit.modules.notification.dto;

public class VerifyEmailResponseDto {

    String email;
    String message;
    Boolean isVerified;

    public VerifyEmailResponseDto() {
    }

    public VerifyEmailResponseDto(String email, String message, Boolean isVerified) {
        this.email = email;
        this.message = message;
        this.isVerified = isVerified;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }   
}
