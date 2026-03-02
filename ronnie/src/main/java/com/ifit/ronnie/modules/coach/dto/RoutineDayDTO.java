package com.ifit.ronnie.modules.coach.dto;

import java.util.List;

public class RoutineDayDTO {

    private Integer dayNumber;

    private String dayName;

    private String description;

    private List<RoutineExerciseDTO> exercises;

    public RoutineDayDTO() {
    }

    public RoutineDayDTO(Integer dayNumber, String dayName, String description,
            List<RoutineExerciseDTO> exercises) {
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

    public List<RoutineExerciseDTO> getExercises() {
        return exercises;
    }

    public void setExercises(List<RoutineExerciseDTO> exercises) {
        this.exercises = exercises;
    }
}
