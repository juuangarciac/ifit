package com.uca.juangarcia.ifit.modules.training.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidad que representa un día específico dentro de una rutina de entrenamiento.
 * 
 * <p>Cada día de rutina contiene:
 * <ul>
 *   <li>Número del día dentro de la rutina (ej: Día 1, Día 2, etc.)</li>
 *   <li>Nombre descriptivo del día (ej: "Tren Superior", "Tren Push")</li>
 *   <li>Lista de ejercicios programados para ese día</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "routine_day")
public class RoutineDay {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;
    
    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;
    
    @Column(name = "day_name", nullable = false, length = 255)
    private String dayName;
    
    @Column(length = 1000)
    private String description;
    
    @OneToMany(mappedBy = "routineDay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoutineExercise> exercises = new ArrayList<>();
    
    // Constructors
    
    public RoutineDay() {}
    
    public RoutineDay(Routine routine, Integer dayNumber, String dayName, String description) {
        this.routine = routine;
        this.dayNumber = dayNumber;
        this.dayName = dayName;
        this.description = description;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Routine getRoutine() {
        return routine;
    }
    
    public void setRoutine(Routine routine) {
        this.routine = routine;
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
    
    public List<RoutineExercise> getExercises() {
        return exercises;
    }
    
    public void setExercises(List<RoutineExercise> exercises) {
        this.exercises = exercises;
    }
    
    // Helper methods
    
    /**
     * Añade un ejercicio al día de entrenamiento.
     * Establece automáticamente la relación bidireccional.
     */
    public void addExercise(RoutineExercise exercise) {
        exercises.add(exercise);
        exercise.setRoutineDay(this);
    }
    
    /**
     * Elimina un ejercicio del día de entrenamiento.
     */
    public void removeExercise(RoutineExercise exercise) {
        exercises.remove(exercise);
        exercise.setRoutineDay(null);
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
        RoutineDay other = (RoutineDay) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "RoutineDay [id=" + id + ", dayNumber=" + dayNumber + ", dayName=" + dayName + 
               ", routineId=" + (routine != null ? routine.getId() : null) + "]";
    }
}
