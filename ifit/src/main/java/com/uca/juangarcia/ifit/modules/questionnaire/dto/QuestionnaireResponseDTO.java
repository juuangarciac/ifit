package com.uca.juangarcia.ifit.modules.questionnaire.dto;

/**
 * DTO for Questionnaire Response - Returned after starting a questionnaire or answering a question
 */
public class QuestionnaireResponseDTO {
    
    private Long responseId;
    private QuestionDTO currentQuestion;
    private Boolean isCompleted;
    private Integer totalQuestionsAnswered;
    
    // Constructors
    public QuestionnaireResponseDTO() {
    }
    
    public QuestionnaireResponseDTO(Long responseId, QuestionDTO currentQuestion, 
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
        private QuestionDTO currentQuestion;
        private Boolean isCompleted;
        private Integer totalQuestionsAnswered;
        
        public Builder responseId(Long responseId) {
            this.responseId = responseId;
            return this;
        }
        
        public Builder currentQuestion(QuestionDTO currentQuestion) {
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
        
        public QuestionnaireResponseDTO build() {
            return new QuestionnaireResponseDTO(responseId, currentQuestion, isCompleted, totalQuestionsAnswered);
        }
    }
    
    // Getters and Setters
    public Long getResponseId() {
        return responseId;
    }
    
    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }
    
    public QuestionDTO getCurrentQuestion() {
        return currentQuestion;
    }
    
    public void setCurrentQuestion(QuestionDTO currentQuestion) {
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
