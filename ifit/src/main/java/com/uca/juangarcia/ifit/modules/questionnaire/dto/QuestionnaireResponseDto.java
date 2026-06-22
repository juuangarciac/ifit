package com.uca.juangarcia.ifit.modules.questionnaire.dto;

/**
 * DTO for Questionnaire Response - Returned after starting a questionnaire or answering a question
 */
public class QuestionnaireResponseDto {
    
    private Long responseId;
    private QuestionDto currentQuestion;
    private Boolean isCompleted;
    private Integer totalQuestionsAnswered;
    
    // Constructors
    public QuestionnaireResponseDto() {
    }
    
    public QuestionnaireResponseDto(Long responseId, QuestionDto currentQuestion, 
                                   Boolean isCompleted, Integer totalQuestionsAnswered) {
        this.responseId = responseId;
        this.currentQuestion = currentQuestion;
        this.isCompleted = isCompleted;
        this.totalQuestionsAnswered = totalQuestionsAnswered;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long responseId;
        private QuestionDto currentQuestion;
        private Boolean isCompleted;
        private Integer totalQuestionsAnswered;
        
        public Builder responseId(Long responseId) {
            this.responseId = responseId;
            return this;
        }
        
        public Builder currentQuestion(QuestionDto currentQuestion) {
            this.currentQuestion = currentQuestion;
            return this;
        }
        
        public Builder isCompleted(Boolean isCompleted) {
            this.isCompleted = isCompleted;
            return this;
        }
        
        public Builder totalQuestionsAnswered(Integer totalQuestionsAnswered) {
            this.totalQuestionsAnswered = totalQuestionsAnswered;
            return this;
        }
        
        public QuestionnaireResponseDto build() {
            return new QuestionnaireResponseDto(responseId, currentQuestion, isCompleted, totalQuestionsAnswered);
        }
    }
    
    // Getters and Setters
    public Long getResponseId() {
        return responseId;
    }
    
    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }
    
    public QuestionDto getCurrentQuestion() {
        return currentQuestion;
    }
    
    public void setCurrentQuestion(QuestionDto currentQuestion) {
        this.currentQuestion = currentQuestion;
    }
    
    public Boolean getIsCompleted() {
        return isCompleted;
    }
    
    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
    
    public Integer getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }
    
    public void setTotalQuestionsAnswered(Integer totalQuestionsAnswered) {
        this.totalQuestionsAnswered = totalQuestionsAnswered;
    }
}
