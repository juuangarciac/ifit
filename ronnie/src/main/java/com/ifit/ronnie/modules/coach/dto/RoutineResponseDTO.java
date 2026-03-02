package com.ifit.ronnie.modules.coach.dto;

import java.util.List;

public class RoutineResponseDTO {

    private String message;

    private String description;

    private Integer trainingDays;

    private List<RoutineDayDTO> days;

    public RoutineResponseDTO() {
    }

    public RoutineResponseDTO(String message, String description, Integer trainingDays,
            List<RoutineDayDTO> days) {
        this.message = message;
        this.description = description;
        this.trainingDays = trainingDays;
        this.days = days;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTrainingDays() {
        return trainingDays;
    }

    public void setTrainingDays(Integer trainingDays) {
        this.trainingDays = trainingDays;
    }

    public List<RoutineDayDTO> getDays() {
        return days;
    }

    public void setDays(List<RoutineDayDTO> days) {
        this.days = days;
    }
}
