package com.uca.juangarcia.ifit.modules.questionnaire.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * QuestionOption entity - Represents an answer option for a question
 * Contains the navigation logic (nextQuestion) for the decision tree
 */
@Entity
@Table(name = "question_option")
public class QuestionOption {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    private Question question;
    
    @Column(nullable = false, length = 255)
    private String text;
    
    @ManyToOne
    @JoinColumn(name = "next_question_id")
    private Question nextQuestion;
    
    @Column(nullable = false)
    private Integer displayOrder = 0;
    
    @Column(nullable = false)
    private Boolean requiresTextInput = false;
    
    @Column(columnDefinition = "TEXT")
    private String textInputPrompt;
    
    @Column(length = 100)
    private String textInputPlaceholder;
    
    // Constructors
    public QuestionOption() {
    }
    
    public QuestionOption(String text, Question nextQuestion, Integer displayOrder) {
        this.text = text;
        this.nextQuestion = nextQuestion;
        this.displayOrder = displayOrder;
    }
    
    public QuestionOption(String text, Question nextQuestion, Integer displayOrder, 
                         Boolean requiresTextInput, String textInputPrompt) {
        this.text = text;
        this.nextQuestion = nextQuestion;
        this.displayOrder = displayOrder;
        this.requiresTextInput = requiresTextInput;
        this.textInputPrompt = textInputPrompt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Question getQuestion() {
        return question;
    }
    
    public void setQuestion(Question question) {
        this.question = question;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public Question getNextQuestion() {
        return nextQuestion;
    }
    
    public void setNextQuestion(Question nextQuestion) {
        this.nextQuestion = nextQuestion;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public Boolean getRequiresTextInput() {
        return requiresTextInput;
    }
    
    public void setRequiresTextInput(Boolean requiresTextInput) {
        this.requiresTextInput = requiresTextInput;
    }
    
    public String getTextInputPrompt() {
        return textInputPrompt;
    }
    
    public void setTextInputPrompt(String textInputPrompt) {
        this.textInputPrompt = textInputPrompt;
    }
    
    public String getTextInputPlaceholder() {
        return textInputPlaceholder;
    }
    
    public void setTextInputPlaceholder(String textInputPlaceholder) {
        this.textInputPlaceholder = textInputPlaceholder;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
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
        QuestionOption other = (QuestionOption) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (text == null) {
            if (other.text != null)
                return false;
        } else if (!text.equals(other.text))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "QuestionOption [id=" + id + ", text=" + text + ", displayOrder=" + displayOrder + "]";
    }
}
