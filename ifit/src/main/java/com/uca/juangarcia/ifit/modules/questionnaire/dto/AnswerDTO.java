package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import java.time.LocalDateTime;

/**
 * DTO for displaying a user's answer
 */
public class AnswerDTO {
    
    private Long answerId;
    private String questionText;
    private String selectedOption;
    private String additionalText;
    private String aiDescription;
    private LocalDateTime answeredAt;
    
    // Constructors
    public AnswerDTO() {
    }
    
    public AnswerDTO(Long answerId, String questionText, String selectedOption, 
                    String additionalText, String aiDescription, LocalDateTime answeredAt) {
        this.answerId = answerId;
        this.questionText = questionText;
        this.selectedOption = selectedOption;
        this.additionalText = additionalText;
        this.aiDescription = aiDescription;
        this.answeredAt = answeredAt;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long answerId;
        private String questionText;
        private String selectedOption;
        private String additionalText;
        private String aiDescription;
        private LocalDateTime answeredAt;
        
        public Builder answerId(Long answerId) {
            this.answerId = answerId;
            return this;
        }
        
        public Builder questionText(String questionText) {
            this.questionText = questionText;
            return this;
        }
        
        public Builder selectedOption(String selectedOption) {
            this.selectedOption = selectedOption;
            return this;
        }
        
        public Builder additionalText(String additionalText) {
            this.additionalText = additionalText;
            return this;
        }
        
        public Builder aiDescription(String aiDescription) {
            this.aiDescription = aiDescription;
            return this;
        }
        
        public Builder answeredAt(LocalDateTime answeredAt) {
            this.answeredAt = answeredAt;
            return this;
        }
        
        public AnswerDTO build() {
            return new AnswerDTO(answerId, questionText, selectedOption, additionalText, aiDescription, answeredAt);
        }
    }
    
    // Getters and Setters
    public Long getAnswerId() {
        return answerId;
    }
    
    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public String getSelectedOption() {
        return selectedOption;
    }
    
    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }
    
    public String getAdditionalText() {
        return additionalText;
    }
    
    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }
    
    public String getAiDescription() {
        return aiDescription;
    }
    
    public void setAiDescription(String aiDescription) {
        this.aiDescription = aiDescription;
    }
    
    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
    
    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
