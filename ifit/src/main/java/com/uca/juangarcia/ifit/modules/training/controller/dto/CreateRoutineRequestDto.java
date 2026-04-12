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
    
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Training days cannot be null")
    @Min(value = 1, message = "Must have at least 1 training day")
    private Integer trainingDays;

    @NotEmpty(message = "Routine must have at least one day")
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
