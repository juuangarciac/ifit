package com.uca.juangarcia.ifit.modules.training.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

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
    
    private String description;
    
    @JsonProperty("trainingDays")
    private Integer trainingDays;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private List<RoutineDayDto> days;
    
    // Constructors
    
    public RoutineResponseDto() {}
    
    public RoutineResponseDto(Long id, Long userId, String description, Integer trainingDays,
                             Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, 
                             List<RoutineDayDto> days) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.trainingDays = trainingDays;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.days = days;
    }
    
    // Getters and Setters
    
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
    
    public List<RoutineDayDto> getDays() {
        return days;
    }
    
    public void setDays(List<RoutineDayDto> days) {
        this.days = days;
    }
}
