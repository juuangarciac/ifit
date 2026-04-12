package com.uca.juangarcia.ifit.modules.training.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.annotations.SQLOrder;
import org.hibernate.annotations.SortNatural;

import com.uca.juangarcia.ifit.modules.user.model.AppUser;

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
 * Entidad que representa una rutina de entrenamiento en el sistema iFit.
 * 
 * <p>Una rutina es un plan de entrenamiento personalizado que incluye:
 * <ul>
 *   <li>Información del usuario al que pertenece</li>
 *   <li>Descripción y objetivos de la rutina</li>
 *   <li>Días de entrenamiento programados</li>
 *   <li>Fecha de creación y última actualización</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "routine")
public class Routine {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "training_days", nullable = false)
    private Integer trainingDays;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private boolean isActive = true;

    @Column(name = "current_day", columnDefinition = "integer default 1")
    private Integer currentDay;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLOrder("dayNumber ASC")
    private List<RoutineDay> days;
    
    // Constructors
    
    public Routine() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Routine(AppUser user, String description, Integer trainingDays) {
        this.user = user;
        this.description = description;
        this.trainingDays = trainingDays;
        this.createdAt = LocalDateTime.now();
    }

    public Routine(LocalDateTime createdAt, Integer currentDay, String description, Long id, Integer trainingDays, LocalDateTime updatedAt, AppUser user) {
        this.createdAt = createdAt;
        this.currentDay = currentDay;
        this.description = description;
        this.id = id;
        this.trainingDays = trainingDays;
        this.updatedAt = updatedAt;
        this.user = user;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public AppUser getUser() {
        return user;
    }
    
    public void setUser(AppUser user) {
        this.user = user;
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
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(Integer currentDay) {
        this.currentDay = currentDay;
    }
    
    public List<RoutineDay> getDays() {
        return days;
    }
    
    public void setDays(List<RoutineDay> days) {
        this.days = days;
    }
    
    // Helper methods
    
    /**
     * Añade un día de entrenamiento a la rutina.
     * Establece automáticamente la relación bidireccional.
     */
    public void addDay(RoutineDay day) {
        days.add(day);
        day.setRoutine(this);
    }
    
    /**
     * Elimina un día de entrenamiento de la rutina.
     */
    public void removeDay(RoutineDay day) {
        days.remove(day);
        day.setRoutine(null);
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
        Routine other = (Routine) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "Routine [id=" + id + ", userId=" + (user != null ? user.getId() : null) + 
               ", description=" + description + ", trainingDays=" + trainingDays + 
               ", isActive=" + isActive + ", createdAt=" + createdAt + "]";
    }
}
