package com.uca.juangarcia.ifit.modules.notification.dto;

public class EmailResponseDto {
    private Boolean isSend;
    private String  message;

    public EmailResponseDto(Boolean isSend, String message) {
        this.isSend = isSend;
        this.message = message;
    }

    public Boolean getisSend() {
        return isSend;
    }

    public void setisSend(Boolean isSend) {
        this.isSend = isSend;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
