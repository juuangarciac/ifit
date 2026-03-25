package com.uca.juangarcia.ifit.modules.training.controller.dto;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de respuesta que contiene todos los datos de una rutina.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class RoutineResponseDto {
    
    private Long id;
    
    private Long userId;
    
    private String message;

    private String description;
    
    private Integer trainingDays;
    
    private Boolean isActive;

    private Integer currentDay;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Set<RoutineDayDto> days;
    
    // Constructors
    
    public RoutineResponseDto() {}

    public RoutineResponseDto(Long id, Long userId, String message, String description, Integer trainingDays, Boolean isActive, Integer currentDay, LocalDateTime createdAt, LocalDateTime updatedAt, Set<RoutineDayDto> days) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.description = description;
        this.trainingDays = trainingDays;
        this.isActive = isActive;
        this.currentDay = currentDay;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.days = days;
    }

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public Integer getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(Integer currentDay) {
        this.currentDay = currentDay;
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
    
    public Set<RoutineDayDto> getDays() {
        return days;
    }
    
    public void setDays(Set<RoutineDayDto> days) {
        this.days = days;
    }
}
