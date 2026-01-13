package com.uca.juangarcia.ifit.shared.exception;


public class QuestionNotFoundException extends Exception{
    private Long id;

    public QuestionNotFoundException(Long id){
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getMessage(){
        return "Question " + id + " does not exists.";
    }
}
