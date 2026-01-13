package com.ifit.ronnie.controller.DTO;

public class MessageDTO {
    
    private Integer memoryId;
    private String message;

    public MessageDTO(Integer memoryId, String message) {
        this.memoryId = memoryId;
        this.message = message;
    }

    public Integer getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(Integer memoryId) {
        this.memoryId = memoryId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
