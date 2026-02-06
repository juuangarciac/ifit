package com.ifit.ronnie.controller.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExcerciseDTO {
    @JsonProperty("exerciseId")
    private String exerciseId;

    @JsonProperty("exerciseName")
    private String exerciseName;

    private int sets;
    private String reps;

    @JsonProperty("restSeconds")
    private int restSeconds;

    private String notes;

}
