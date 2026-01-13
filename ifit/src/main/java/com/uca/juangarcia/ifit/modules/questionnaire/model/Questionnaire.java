package com.uca.juangarcia.ifit.modules.questionnaire.model;

import java.time.LocalDateTime;

import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;
import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Questionnaire entity - Entry point for a questionnaire
 * Contains the first question to start the decision tree flow
 */
@Entity
@Table(name = "questionnaire")
public class Questionnaire {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "coach_model_type_id")
    private CoachModelType coachModelType;
    
    @ManyToOne
    @JoinColumn(name = "experience_level_id")
    private ExperienceLevel experienceLevel;
    
    @ManyToOne
    @JoinColumn(name = "first_question_id")
    private Question firstQuestion;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean isEnabled = true;
    
    // Constructors
    public Questionnaire() {
    }
    
    public Questionnaire(String name, String description, CoachModelType coachModelType, 
                        ExperienceLevel experienceLevel, Question firstQuestion) {
        this.name = name;
        this.description = description;
        this.coachModelType = coachModelType;
        this.experienceLevel = experienceLevel;
        this.firstQuestion = firstQuestion;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isEnabled == null) {
            isEnabled = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public CoachModelType getCoachModelType() {
        return coachModelType;
    }
    
    public void setCoachModelType(CoachModelType coachModelType) {
        this.coachModelType = coachModelType;
    }
    
    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }
    
    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }
    
    public Question getFirstQuestion() {
        return firstQuestion;
    }
    
    public void setFirstQuestion(Question firstQuestion) {
        this.firstQuestion = firstQuestion;
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
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Questionnaire other = (Questionnaire) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "Questionnaire [id=" + id + ", name=" + name + ", description=" + description + "]";
    }
}
