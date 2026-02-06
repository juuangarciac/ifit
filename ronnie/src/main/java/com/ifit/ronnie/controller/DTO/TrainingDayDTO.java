package com.ifit.ronnie.controller.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class TrainingDayDTO {
    @JsonProperty("dayNumber")
    private int dayNumber;

    @JsonProperty("dayName")
    private String dayName;

    private List<ExcerciseDTO> exercises;
}
