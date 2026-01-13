package com.uca.juangarcia.ifit.modules.notification.dto;

public class VerifyEmailRequestDto {
    String email;
    String verificationCode;

    public VerifyEmailRequestDto() {
    }

    public VerifyEmailRequestDto(String email, String verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
