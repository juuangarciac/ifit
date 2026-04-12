package com.uca.juangarcia.ifit.modules.training.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineDayDto;
import com.uca.juangarcia.ifit.modules.training.model.RoutineDay;

/**
 * Mapper para convertir entre entidades RoutineDay y sus DTOs.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Component
public class RoutineDayMapper {
    
    private final RoutineExerciseMapper exerciseMapper;
    
    public RoutineDayMapper(RoutineExerciseMapper exerciseMapper) {
        this.exerciseMapper = exerciseMapper;
    }
    
    /**
     * Convierte una entidad RoutineDay a DTO incluyendo sus ejercicios.
     */
    public RoutineDayDto toDto(RoutineDay day) {
        if (day == null) {
            return null;
        }
        
        return new RoutineDayDto(
            day.getId(),
            day.getDayNumber(),
            day.getDayName(),
            day.getDescription(),
            day.getExercises().stream()
                .map(exerciseMapper::toDto)
                .collect(Collectors.toList())
        );
    }
    
    /**
     * Convierte un DTO a entidad RoutineDay.
     * No establece la Routine - debe hacerse en el servicio.
     */
    public RoutineDay toEntity(RoutineDayDto dto) {
        if (dto == null) {
            return null;
        }
        
        RoutineDay day = new RoutineDay();
        day.setId(dto.getId());
        day.setDayNumber(dto.getDayNumber());
        day.setDayName(dto.getDayName());
        day.setDescription(dto.getDescription());
        
        // Convertir ejercicios y establecer la relación bidireccional
        if (dto.getExercises() != null) {
            dto.getExercises().forEach(exerciseDto -> {
                var exercise = exerciseMapper.toEntity(exerciseDto);
                day.addExercise(exercise);
            });
        }
        
        return day;
    }
    
    /**
     * Convierte una lista de entidades a lista de DTOs.
     */
    public List<RoutineDayDto> toDtoList(List<RoutineDay> days) {
        if (days == null) {
            return null;
        }
        
        return days.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
