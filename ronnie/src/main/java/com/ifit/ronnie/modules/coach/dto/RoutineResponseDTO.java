package com.ifit.ronnie.modules.coach.dto;

import java.util.List;

public class RoutineResponseDto {

    private String message;

    private String description;

    private Integer trainingDays;

    private List<RoutineDayDto> days;

    public RoutineResponseDto() {
    }

    public RoutineResponseDto(String message, String description, Integer trainingDays,
            List<RoutineDayDto> days) {
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

    public List<RoutineDayDto> getDays() {
        return days;
    }

    public void setDays(List<RoutineDayDto> days) {
        this.days = days;
    }
}
