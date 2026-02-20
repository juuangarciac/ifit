package com.ifit.ronnie.controller.dto.routine;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class RonnieRoutineDayDTO {

    private Integer dayNumber;

    private String dayName;

    private String description;

    private List<RonnieRoutineExerciseDTO> exercises;

    public RonnieRoutineDayDTO() {
    }

    public RonnieRoutineDayDTO(Integer dayNumber, String dayName, String description,
            List<RonnieRoutineExerciseDTO> exercises) {
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

    public List<RonnieRoutineExerciseDTO> getExercises() {
        return exercises;
    }

    public void setExercises(List<RonnieRoutineExerciseDTO> exercises) {
        this.exercises = exercises;
    }
}
