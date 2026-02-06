package com.ifit.ronnie.controller.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoutineDTO {
    private String userId;
    private String description;

    @JsonProperty("trainingDays")
    private int trainingDays;

    private List<TrainingDayDTO> days;
}
