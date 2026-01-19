package com.uca.juangarcia.ifit.exception.dto;

public class ExperienceLevelNotFoundException extends Exception {

    private Long id;

    public ExperienceLevelNotFoundException(Long id){
        this.id = id;
    }

    public ExperienceLevelNotFoundException(String string) {
        //TODO Auto-generated constructor stub
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getMessage(){
        return "ExperienceLevelType " + " does not exists.";
    }
}
