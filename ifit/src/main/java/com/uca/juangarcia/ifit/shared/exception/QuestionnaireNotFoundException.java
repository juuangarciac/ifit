package com.uca.juangarcia.ifit.shared.exception;

public class QuestionnaireNotFoundException extends Exception {
  private Long id;

    public QuestionnaireNotFoundException(Long id) {
        super("Questionnaire with id " + id + " does not exist.");
        this.id = id;
    }

    public QuestionnaireNotFoundException(String message) {
        super(message);
        this.id = null; 
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return "Questionnaire with id " + id + " does not exist.";
    }
}
