package com.uca.juangarcia.ifit.modules.training.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineResponseDto;
import com.uca.juangarcia.ifit.modules.training.model.Routine;

/**
 * Mapper para convertir entre entidades Routine y sus DTOs.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Component
public class RoutineMapper {
    
    private final RoutineDayMapper dayMapper;
    
    public RoutineMapper(RoutineDayMapper dayMapper) {
        this.dayMapper = dayMapper;
    }
    
    /**
     * Convierte una entidad Routine completa a DTO de respuesta.
     */
    public RoutineResponseDto toResponseDto(Routine routine) {
        if (routine == null) {
            return null;
        }
        
        return new RoutineResponseDto(
            routine.getId(),
            routine.getUser() != null ? routine.getUser().getId() : null,
            routine.getDescription(),
            routine.getTrainingDays(),
            routine.isActive(),
            routine.getCreatedAt(),
            routine.getUpdatedAt(),
            dayMapper.toDtoList(routine.getDays())
        );
    }
    
    /**
     * Convierte una lista de entidades Routine a lista de DTOs de respuesta.
     */
    public List<RoutineResponseDto> toResponseDtoList(List<Routine> routines) {
        if (routines == null) {
            return null;
        }
        
        return routines.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }
    
    /**
     * Convierte una página de entidades Routine a página de DTOs de respuesta.
     */
    public Page<RoutineResponseDto> toResponseDtoPage(Page<Routine> routinePage) {
        if (routinePage == null) {
            return null;
        }
        
        return routinePage.map(this::toResponseDto);
    }
}
