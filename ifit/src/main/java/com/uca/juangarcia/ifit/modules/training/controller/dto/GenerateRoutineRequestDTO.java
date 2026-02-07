package com.uca.juangarcia.ifit.modules.training.controller.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitar la generación de una rutina personalizada.
 * 
 * El frontend envía el userId y el responseId del cuestionario completado.
 * El backend construye el prompt y llama al servicio de IA.
 */
public class GenerateRoutineRequestDTO {
    
    @NotNull(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Response ID is required")
    private Long responseId;
    
    // Constructors
    public GenerateRoutineRequestDTO() {
    }
    
    public GenerateRoutineRequestDTO(String userId, Long responseId) {
        this.userId = userId;
        this.responseId = responseId;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Long getResponseId() {
        return responseId;
    }
    
    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }
    
    @Override
    public String toString() {
        return "GenerateRoutineRequestDTO{" +
                "userId='" + userId + '\'' +
                ", responseId=" + responseId +
                '}';
    }
}