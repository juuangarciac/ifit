package com.uca.juangarcia.ifit.modules.training.controller.dto;

import java.util.List;

/**
 * DTO para representar un día dentro de una rutina de entrenamiento.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class RoutineDayDto {
    
    private Long id;
    
    private Integer dayNumber;
    
    private String dayName;
    
    private String description;
    
    private List<RoutineExerciseDto> exercises;
    
    // Constructors
    
    public RoutineDayDto() {}
    
    public RoutineDayDto(Long id, Integer dayNumber, String dayName, String description, 
                        List<RoutineExerciseDto> exercises) {
        this.id = id;
        this.dayNumber = dayNumber;
        this.dayName = dayName;
        this.description = description;
        this.exercises = exercises;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getDayNumber() {
        return dayNumber;
    }
    
    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }
    
    public String getDayName() {
        return dayName;
    }
    
    public void setDayName(String dayName) {
        this.dayName = dayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<RoutineExerciseDto> getExercises() {
        return exercises;
    }
    
    public void setExercises(List<RoutineExerciseDto> exercises) {
        this.exercises = exercises;
    }
}
