package com.uca.juangarcia.ifit.exception;

public class QuestionnaireQuestionNotFoundException extends Exception{
    private Long id;

    public QuestionnaireQuestionNotFoundException(Long id) {
        super("QuestionnaireQuestion with id " + id + " does not exist.");
        this.id = id;
    }

    public QuestionnaireQuestionNotFoundException(String message) {
        super(message);
        this.id = null; 
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return "QuestionnaireQuestion with id " + id + " does not exist.";
    }
}
