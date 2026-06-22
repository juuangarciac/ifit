package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for submitting an answer to a question
 */
public class AnswerRequestDto {
    
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    @NotNull(message = "Selected option ID is required")
    private Long selectedOptionId;
    
    private String additionalText;
    
    // Constructors
    public AnswerRequestDto() {
    }
    
    public AnswerRequestDto(Long questionId, Long selectedOptionId, String additionalText) {
        this.questionId = questionId;
        this.selectedOptionId = selectedOptionId;
        this.additionalText = additionalText;
    }
    
    // Getters and Setters
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public Long getSelectedOptionId() {
        return selectedOptionId;
    }
    
    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }
    
    public String getAdditionalText() {
        return additionalText;
    }
    
    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }
}
