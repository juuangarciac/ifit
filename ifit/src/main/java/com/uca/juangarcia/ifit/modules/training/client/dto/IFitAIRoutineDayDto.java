package com.uca.juangarcia.ifit.modules.training.client.dto;

import java.util.List;

import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineDayDto;

public class IFitAIRoutineDayDto {

    private Integer dayNumber;

    private String dayName;

    private String description;

    private List<IFitAIRoutineExerciseDto> exercises;

    public IFitAIRoutineDayDto() {
    }

    public IFitAIRoutineDayDto(Integer dayNumber, String dayName, String description,
            List<IFitAIRoutineExerciseDto> exercises) {
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

    public List<IFitAIRoutineExerciseDto> getExercises() {
        return exercises;
    }

    public void setExercises(List<IFitAIRoutineExerciseDto> exercises) {
        this.exercises = exercises;
    }

    /**
     * Convierte este DTO a un RoutineDayDto para su uso en la lógica de negocio.
     * 
     * @return
     */
    public RoutineDayDto toRoutineDayDto() {
        RoutineDayDto dto = new RoutineDayDto();
        dto.setDayNumber(this.dayNumber);
        dto.setDayName(this.dayName);
        dto.setDescription(this.description);
        if (this.exercises != null) {
            dto.setExercises(this.exercises.stream().map(IFitAIRoutineExerciseDto::toRoutineExerciseDto).toList());
        }
        return dto;
    }
}
