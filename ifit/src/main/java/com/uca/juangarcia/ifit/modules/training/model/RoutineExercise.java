package com.uca.juangarcia.ifit.modules.training.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entidad que representa un ejercicio dentro de un día de rutina.
 * 
 * <p>Cada ejercicio incluye:
 * <ul>
 *   <li>Identificador del ejercicio de la base de datos de ejercicios</li>
 *   <li>Nombre del ejercicio</li>
 *   <li>Configuración de series, repeticiones y descanso</li>
 *   <li>Notas y recomendaciones específicas</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "routine_exercise")
public class RoutineExercise {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_day_id", nullable = false)
    private RoutineDay routineDay;
    
    @Column(name = "exercise_name", nullable = false, length = 255)
    private String exerciseName;
    
    @Column(nullable = true)
    private Integer sets;
    
    @Column(nullable = true, length = 50)
    private String reps;
    
    @Column(name = "rest_seconds", nullable = true)
    private Integer restSeconds;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(name = "order_index")
    private Integer orderIndex;
    
    // Constructors
    
    public RoutineExercise() {}
    
    public RoutineExercise(RoutineDay routineDay, String exerciseName, Integer sets, String reps, Integer restSeconds) {
        this.routineDay = routineDay;
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.restSeconds = restSeconds;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public RoutineDay getRoutineDay() {
        return routineDay;
    }
    
    public void setRoutineDay(RoutineDay routineDay) {
        this.routineDay = routineDay;
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
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RoutineExercise other = (RoutineExercise) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "RoutineExercise [id=" + id + ", exerciseName=" + exerciseName + ", sets=" + sets + ", reps=" + reps + 
               ", restSeconds=" + restSeconds + "]";
    }
}
