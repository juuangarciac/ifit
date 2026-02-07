package com.uca.juangarcia.ifit.modules.training.controller.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la creación de una nueva rutina de entrenamiento.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class CreateRoutineRequestDto {
    
    @NotNull(message = "El ID del usuario no puede ser nulo")
    private Long userId;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String description;
    
    @NotNull(message = "Los días de entrenamiento no pueden ser nulos")
    @Min(value = 1, message = "Debe haber al menos 1 día de entrenamiento")
    private Integer trainingDays;
    
    @NotEmpty(message = "La rutina debe tener al menos un día")
    @Valid
    private List<RoutineDayDto> days;
    
    // Constructors
    
    public CreateRoutineRequestDto() {}
    
    public CreateRoutineRequestDto(Long userId, String description, Integer trainingDays, 
                                  List<RoutineDayDto> days) {
        this.userId = userId;
        this.description = description;
        this.trainingDays = trainingDays;
        this.days = days;
    }
    
    // Getters and Setters
    
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
    
    public List<RoutineDayDto> getDays() {
        return days;
    }
    
    public void setDays(List<RoutineDayDto> days) {
        this.days = days;
    }
}
