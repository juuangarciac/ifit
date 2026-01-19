package com.uca.juangarcia.ifit.exception.dto;

public class CoachModelTypeNotFoundException extends Exception {
    private String coachId;
    
    public CoachModelTypeNotFoundException(String coachId) {
        this.coachId = coachId;
    }

    public String getCoachId() {
        return coachId;
    }

    public void setCoachId(String coachId) {
        this.coachId = coachId;
    }

    @Override
    public String getMessage() {
        return "Coach with ID " + coachId + " not found.";
    }
}
