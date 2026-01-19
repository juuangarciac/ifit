package com.uca.juangarcia.ifit.exception.dto;

public class UserQuestionnaireNotFoundException extends Exception {
    private Long userId;

    public UserQuestionnaireNotFoundException(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String getMessage() {
        return "Questionnaire for User with ID " + userId + " not found.";
    }
}
