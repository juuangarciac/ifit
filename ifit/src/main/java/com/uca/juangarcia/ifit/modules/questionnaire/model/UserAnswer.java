package com.uca.juangarcia.ifit.modules.questionnaire.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * UserAnswer entity - Represents a user's answer to a specific question
 * Links the user's choice (selectedOption) with optional additional text input
 */
@Entity
@Table(name = "user_answer")
public class UserAnswer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "response_id", nullable = false)
    @JsonBackReference
    private QuestionnaireResponse response;
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;
    
    @Column(columnDefinition = "TEXT")
    private String additionalText;
    
    @Column(columnDefinition = "TEXT")
    private String aiGeneratedDescription;
    
    @Column(nullable = false)
    private LocalDateTime answeredAt;
    
    // Constructors
    public UserAnswer() {
    }
    
    public UserAnswer(QuestionnaireResponse response, Question question, 
                     QuestionOption selectedOption, String additionalText) {
        this.response = response;
        this.question = question;
        this.selectedOption = selectedOption;
        this.additionalText = additionalText;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        answeredAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public QuestionnaireResponse getResponse() {
        return response;
    }
    
    public void setResponse(QuestionnaireResponse response) {
        this.response = response;
    }
    
    public Question getQuestion() {
        return question;
    }
    
    public void setQuestion(Question question) {
        this.question = question;
    }
    
    public QuestionOption getSelectedOption() {
        return selectedOption;
    }
    
    public void setSelectedOption(QuestionOption selectedOption) {
        this.selectedOption = selectedOption;
    }
    
    public String getAdditionalText() {
        return additionalText;
    }
    
    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }
    
    public String getAiGeneratedDescription() {
        return aiGeneratedDescription;
    }
    
    public void setAiGeneratedDescription(String aiGeneratedDescription) {
        this.aiGeneratedDescription = aiGeneratedDescription;
    }
    
    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
    
    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((question == null) ? 0 : question.hashCode());
        result = prime * result + ((response == null) ? 0 : response.hashCode());
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
        UserAnswer other = (UserAnswer) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (question == null) {
            if (other.question != null)
                return false;
        } else if (!question.equals(other.question))
            return false;
        if (response == null) {
            if (other.response != null)
                return false;
        } else if (!response.equals(other.response))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "UserAnswer [id=" + id + ", questionId=" + (question != null ? question.getId() : null) 
                + ", selectedOptionId=" + (selectedOption != null ? selectedOption.getId() : null) + "]";
    }
}
