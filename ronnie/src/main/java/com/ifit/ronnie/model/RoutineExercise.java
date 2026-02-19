package com.ifit.ronnie.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para representar un ejercicio dentro de una rutina.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
public class RoutineExercise {
    
    private Long id;
    
    @NotBlank(message = "El ID del ejercicio no puede estar vacío")
    private String exerciseId;
    
    @NotBlank(message = "El nombre del ejercicio no puede estar vacío")
    private String exerciseName;
    
    @NotNull(message = "Las series no pueden ser nulas")
    @Min(value = 1, message = "Debe haber al menos 1 serie")
    private Integer sets;
    
    @NotBlank(message = "Las repeticiones no pueden estar vacías")
    private String reps;
    
    @NotNull(message = "El descanso no puede ser nulo")
    @Min(value = 0, message = "El descanso debe ser 0 o mayor")
    private Integer restSeconds;
    
    private String notes;
    
    private Integer orderIndex;
    
    // Constructors
    
    public RoutineExercise() {}
    
    public RoutineExercise(Long id, String exerciseId, String exerciseName, Integer sets, 
                             String reps, Integer restSeconds, String notes, Integer orderIndex) {
        this.id = id;
        this.exerciseId = exerciseId;
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
    
    public String getExerciseId() {
        return exerciseId;
    }
    
    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }
    
    public String getExerciseName() {
        return exerciseName;
    }
    
    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
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
