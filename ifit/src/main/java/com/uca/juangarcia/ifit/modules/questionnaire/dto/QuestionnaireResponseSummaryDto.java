package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Questionnaire Response Summary - Complete view of a user's questionnaire completion
 */
public class QuestionnaireResponseSummaryDto {
    
    private Long responseId;
    private Long userId;
    private String userName;
    private Long questionnaireId;
    private String questionnaireName;
    private String questionnaireDescription;
    private List<AnswerDto> answers;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Boolean isCompleted;
    
    // Constructors
    public QuestionnaireResponseSummaryDto() {
    }
    
    public QuestionnaireResponseSummaryDto(Long responseId, Long userId, String userName,
                                          Long questionnaireId, String questionnaireName, 
                                          String questionnaireDescription, List<AnswerDto> answers,
                                          LocalDateTime startedAt, LocalDateTime completedAt, 
                                          Boolean isCompleted) {
        this.responseId = responseId;
        this.userId = userId;
        this.userName = userName;
        this.questionnaireId = questionnaireId;
        this.questionnaireName = questionnaireName;
        this.questionnaireDescription = questionnaireDescription;
        this.answers = answers;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.isCompleted = isCompleted;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long responseId;
        private Long userId;
        private String userName;
        private Long questionnaireId;
        private String questionnaireName;
        private String questionnaireDescription;
        private List<AnswerDto> answers;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Boolean isCompleted;
        
        public Builder responseId(Long responseId) {
            this.responseId = responseId;
            return this;
        }
        
        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }
        
        public Builder questionnaireId(Long questionnaireId) {
            this.questionnaireId = questionnaireId;
            return this;
        }
        
        public Builder questionnaireName(String questionnaireName) {
            this.questionnaireName = questionnaireName;
            return this;
        }
        
        public Builder questionnaireDescription(String questionnaireDescription) {
            this.questionnaireDescription = questionnaireDescription;
            return this;
        }
        
        public Builder answers(List<AnswerDto> answers) {
            this.answers = answers;
            return this;
        }
        
        public Builder startedAt(LocalDateTime startedAt) {
            this.startedAt = startedAt;
            return this;
        }
        
        public Builder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }
        
        public Builder isCompleted(Boolean isCompleted) {
            this.isCompleted = isCompleted;
            return this;
        }
        
        public QuestionnaireResponseSummaryDto build() {
            return new QuestionnaireResponseSummaryDto(responseId, userId, userName, questionnaireId,
                questionnaireName, questionnaireDescription, answers, startedAt, completedAt, isCompleted);
        }
    }
    
    // Getters and Setters
    public Long getResponseId() {
        return responseId;
    }
    
    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public Long getQuestionnaireId() {
        return questionnaireId;
    }
    
    public void setQuestionnaireId(Long questionnaireId) {
        this.questionnaireId = questionnaireId;
    }
    
    public String getQuestionnaireName() {
        return questionnaireName;
    }
    
    public void setQuestionnaireName(String questionnaireName) {
        this.questionnaireName = questionnaireName;
    }
    
    public String getQuestionnaireDescription() {
        return questionnaireDescription;
    }
    
    public void setQuestionnaireDescription(String questionnaireDescription) {
        this.questionnaireDescription = questionnaireDescription;
    }
    
    public List<AnswerDto> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<AnswerDto> answers) {
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
}
