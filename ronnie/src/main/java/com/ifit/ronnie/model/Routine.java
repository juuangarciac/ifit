package com.ifit.ronnie.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de respuesta que contiene todos los datos de una rutina.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class Routine {
    
    private String message;

    private String description;
    
    @JsonProperty("trainingDays")
    private Integer trainingDays;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private List<RoutineDay> days;
    
    // Constructors
    
    public Routine() {}

    public Routine(String message, String description, Integer trainingDays,
            Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, List<RoutineDay> days) {
        this.message = message;
        this.description = description;
        this.trainingDays = trainingDays;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.days = days;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getTrainingDays() {
        return trainingDays;
    }
    
    public void setTrainingDays(Integer trainingDays) {
        this.trainingDays = trainingDays;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<RoutineDay> getDays() {
        return days;
    }
    
    public void setDays(List<RoutineDay> days) {
        this.days = days;
    }
}
