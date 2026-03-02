package com.uca.juangarcia.ifit.modules.training.mapper;

import org.springframework.stereotype.Component;

import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineExerciseDto;
import com.uca.juangarcia.ifit.modules.training.model.RoutineExercise;


/**
 * Mapper para convertir entre entidades RoutineExercise y sus DTOs.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Component
public class RoutineExerciseMapper {
    
    /**
     * Convierte una entidad RoutineExercise a DTO.
     */
    public RoutineExerciseDto toDto(RoutineExercise exercise) {
        if (exercise == null) {
            return null;
        }
        
        return new RoutineExerciseDto(
            exercise.getId(),
            exercise.getExerciseName(),
            exercise.getSets(),
            exercise.getReps(),
            exercise.getRestSeconds(),
            exercise.getNotes(),
            exercise.getOrderIndex()
        );
    }
    
    /**
     * Convierte un DTO a entidad RoutineExercise.
     * No establece el RoutineDay - debe hacerse en el servicio.
     */
    public RoutineExercise toEntity(RoutineExerciseDto dto) {
        if (dto == null) {
            return null;
        }
        
        RoutineExercise exercise = new RoutineExercise();
        exercise.setId(dto.getId());

        exercise.setExerciseName(dto.getExerciseName());
        exercise.setSets(dto.getSets());
        exercise.setReps(dto.getReps());
        exercise.setRestSeconds(dto.getRestSeconds());
        exercise.setNotes(dto.getNotes());
        exercise.setOrderIndex(dto.getOrderIndex());
        
        return exercise;
    }
    
    /**
     * Actualiza una entidad existente con datos del DTO.
     */
    public void updateEntityFromDto(RoutineExercise exercise, RoutineExerciseDto dto) {
        if (exercise == null || dto == null) {
            return;
        }

        exercise.setExerciseName(dto.getExerciseName());
        exercise.setSets(dto.getSets());
        exercise.setReps(dto.getReps());
        exercise.setRestSeconds(dto.getRestSeconds());
        exercise.setNotes(dto.getNotes());
        exercise.setOrderIndex(dto.getOrderIndex());
    }
}
