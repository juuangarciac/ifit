package com.ifit.ronnie.modules.coach.dto;

import java.util.List;

public class RoutineDayDto {

    private Integer dayNumber;

    private String dayName;

    private String description;

    private List<RoutineExerciseDto> exercises;

    public RoutineDayDto() {
    }

    public RoutineDayDto(Integer dayNumber, String dayName, String description,
            List<RoutineExerciseDto> exercises) {
        this.dayNumber = dayNumber;
        this.dayName = dayName;
        this.description = description;
        this.exercises = exercises;
    }

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RoutineExerciseDto> getExercises() {
        return exercises;
    }

    public void setExercises(List<RoutineExerciseDto> exercises) {
        this.exercises = exercises;
    }
}
