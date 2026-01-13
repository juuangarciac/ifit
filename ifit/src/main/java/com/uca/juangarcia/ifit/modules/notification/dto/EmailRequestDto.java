package com.uca.juangarcia.ifit.modules.notification.dto;

public class EmailRequestDto {
    private String email;

    public EmailRequestDto() {
    }

    public EmailRequestDto(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
