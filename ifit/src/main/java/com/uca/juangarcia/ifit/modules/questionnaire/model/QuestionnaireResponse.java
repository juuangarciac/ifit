package com.uca.juangarcia.ifit.modules.questionnaire.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * QuestionnaireResponse entity - Represents a user's session completing a questionnaire
 * Contains all the user's answers for this questionnaire attempt
 */
@Entity
@Table(name = "questionnaire_response")
public class QuestionnaireResponse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private AppUser user;
    
    @ManyToOne
    @JoinColumn(name = "questionnaire_id", nullable = false)
    private Questionnaire questionnaire;
    
    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserAnswer> answers = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime startedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    @Column(nullable = false)
    private Boolean isCompleted = false;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public QuestionnaireResponse() {
    }
    
    public QuestionnaireResponse(AppUser user, Questionnaire questionnaire) {
        this.user = user;
        this.questionnaire = questionnaire;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        if (isCompleted == null) {
            isCompleted = false;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
    
    // Helper methods
    public void addAnswer(UserAnswer answer) {
        answers.add(answer);
        answer.setResponse(this);
    }
    
    public void removeAnswer(UserAnswer answer) {
        answers.remove(answer);
        answer.setResponse(null);
    }
    
    public void markAsCompleted() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
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
    
    public Questionnaire getQuestionnaire() {
        return questionnaire;
    }
    
    public void setQuestionnaire(Questionnaire questionnaire) {
        this.questionnaire = questionnaire;
    }
    
    public List<UserAnswer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<UserAnswer> answers) {
        this.answers = answers;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        result = prime * result + ((questionnaire == null) ? 0 : questionnaire.hashCode());
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
        QuestionnaireResponse other = (QuestionnaireResponse) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.equals(other.user))
            return false;
        if (questionnaire == null) {
            if (other.questionnaire != null)
                return false;
        } else if (!questionnaire.equals(other.questionnaire))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "QuestionnaireResponse [id=" + id + ", userId=" + (user != null ? user.getId() : null) 
                + ", questionnaireId=" + (questionnaire != null ? questionnaire.getId() : null) 
                + ", isCompleted=" + isCompleted + "]";
    }
}
