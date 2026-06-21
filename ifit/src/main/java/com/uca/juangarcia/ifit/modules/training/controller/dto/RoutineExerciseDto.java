package com.uca.juangarcia.ifit.modules.training.controller.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

/**
 * DTO para representar un ejercicio dentro de una rutina.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class RoutineExerciseDto {
    
    private Long id;

    private String exerciseName;

    /** Id del ejercicio en el catálogo si el nombre se reconcilió; null si no. */
    private Long exerciseId;

    private Integer sets;
    
    private String reps;
    
    private Integer restSeconds;
    
    private String notes;
    
    private Integer orderIndex;
    
    // Constructors
    
    public RoutineExerciseDto() {}
    
    public RoutineExerciseDto(Long id, String exerciseName, Integer sets, String reps, Integer restSeconds, String notes, Integer orderIndex) {
        this.id = id;
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
        this.notes = notes;
        this.orderIndex = orderIndex;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }
    
    public Integer getSets() {
        return sets;
    }
    
    public void setSets(Integer sets) {
        this.sets = sets;
    }
    
    public String getReps() {
        return reps;
    }
    
    public void setReps(String reps) {
        this.reps = reps;
    }
    
    public Integer getRestSeconds() {
        return restSeconds;
    }
    
    public void setRestSeconds(Integer restSeconds) {
        this.restSeconds = restSeconds;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Integer getOrderIndex() {
        return orderIndex;
    }
    
    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
