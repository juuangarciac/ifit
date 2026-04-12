package com.uca.juangarcia.ifit.modules.training.controller.dto;

/**
 * DTO para enviar mensajes al servicio de Ronnie.
 * Estructura requerida por el endpoint /ronnie/generate-routine
 */
public class RonnieMessageDto {
    
    private int memoryId;
    private String message;
    
    // Constructors
    public RonnieMessageDto() {
    }
    
    public RonnieMessageDto(int memoryId, String message) {
        this.memoryId = memoryId;
        this.message = message;
    }
    
    // Getters and Setters
    public int getMemoryId() {
        return memoryId;
    }
    
    public void setMemoryId(int memoryId) {
        this.memoryId = memoryId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return "RonnieMessageDto{" +
                "memoryId=" + memoryId +
                ", message='" + message + '\'' +
                '}';
    }
}