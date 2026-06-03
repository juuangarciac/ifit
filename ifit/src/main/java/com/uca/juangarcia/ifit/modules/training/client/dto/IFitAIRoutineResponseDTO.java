package com.uca.juangarcia.ifit.modules.training.client.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineResponseDto;

public class IFitAIRoutineResponseDto {

    private String message;

    private String description;

    private Integer trainingDays;

    private List<IFitAIRoutineDayDto> days;

    public IFitAIRoutineResponseDto() {
    }

    public IFitAIRoutineResponseDto(String message, String description, Integer trainingDays,
            List<IFitAIRoutineDayDto> days) {
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

    public List<IFitAIRoutineDayDto> getDays() {
        return days;
    }

    public void setDays(List<IFitAIRoutineDayDto> days) {
        this.days = days;
    }

    /**
     * Convierte este DTO de respuesta de Ronnie a un DTO de respuesta de rutina
     * genérico.
     * 
     * @return
     */
    public RoutineResponseDto toRoutineResponseDto() {
        RoutineResponseDto routineResponseDto = new RoutineResponseDto();
        routineResponseDto.setUserId(null);
        routineResponseDto.setMessage(this.message);
        routineResponseDto.setDescription(this.description);
        routineResponseDto.setTrainingDays(this.trainingDays);
        routineResponseDto.setIsActive(false);
        routineResponseDto.setDeleted(false);
        routineResponseDto.setCreatedAt(LocalDateTime.now());
        routineResponseDto.setUpdatedAt(null);
        routineResponseDto.setDays(this.days.stream().map(IFitAIRoutineDayDto::toRoutineDayDto).collect(Collectors.toList()));
        return routineResponseDto;
    }
}
