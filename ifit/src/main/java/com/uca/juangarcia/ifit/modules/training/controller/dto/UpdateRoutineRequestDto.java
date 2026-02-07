package com.uca.juangarcia.ifit.modules.training.controller.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización de una rutina existente.
 * Todos los campos son opcionales para permitir actualizaciones parciales.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class UpdateRoutineRequestDto {
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;
    
    @Min(value = 1, message = "Debe haber al menos 1 día de entrenamiento")
    private Integer trainingDays;
    
    private Boolean isActive;
    
    @Valid
    private List<RoutineDayDto> days;
    
    // Constructors
    
    public UpdateRoutineRequestDto() {}
    
    public UpdateRoutineRequestDto(String description, Integer trainingDays, Boolean isActive, 
                                  List<RoutineDayDto> days) {
        this.description = description;
        this.trainingDays = trainingDays;
        this.isActive = isActive;
        this.days = days;
    }
    
    // Getters and Setters
    
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
    
    public List<RoutineDayDto> getDays() {
        return days;
    }
    
    public void setDays(List<RoutineDayDto> days) {
        this.days = days;
    }
}
