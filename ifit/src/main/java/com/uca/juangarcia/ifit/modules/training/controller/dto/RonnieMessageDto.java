package com.uca.juangarcia.ifit.modules.training.controller.dto;

/**
 * DTO para enviar mensajes al servicio de Ronnie.
 * Estructura requerida por el endpoint /ronnie/generate-routine
 */
public class RonnieMessageDto {

    private int memoryId;
    private String message;
    private String userId;

    // Constructors
    public RonnieMessageDto() {
    }

    public RonnieMessageDto(int memoryId, String message, String userId) {
        this.memoryId = memoryId;
        this.message = message;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "RonnieMessageDto{" +
                "memoryId=" + memoryId +
                ", message='" + message + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}