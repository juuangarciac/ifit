package com.uca.juangarcia.ifit.modules.training.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineDayDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineExerciseDto;
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
            "Bienvenido a tu nueva rutina personalizada. Esta rutina ha sido diseñada específicamente para ti, "
            + "teniendo en cuenta tus objetivos, nivel de experiencia y preferencias. Asegúrate de seguirla de "
            + "manera consistente para obtener los mejores resultados. ¡Vamos a por ello!", 
            routine.getDescription(),
            routine.getTrainingDays(),
            routine.isActive(),
            routine.getCurrentDay(),
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

    /**
 * Convierte un JSON de Ronnie a DTO de respuesta.
 */
public RoutineResponseDto ronnieJsonToRoutineResponseDto(String ronnieJson) {
    try {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Primero deserializamos el String escapado
        String cleanJson = objectMapper.readValue(ronnieJson, String.class);
        
        // Ahora parseamos el JSON real
        JsonNode rootNode = objectMapper.readTree(cleanJson);
        
        // Extraer el nodo "routine"
        JsonNode routineNode = rootNode.get("routine");
        
        // Crear el RoutineResponseDto
        RoutineResponseDto responseDto = new RoutineResponseDto();
        
        // Mapear campos básicos
        responseDto.setMessage(routineNode.get("message").asText());
        responseDto.setDescription(routineNode.get("description").asText());
        responseDto.setTrainingDays(routineNode.get("trainingDays").asInt());
        responseDto.setIsActive(true);
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setUpdatedAt(LocalDateTime.now());
        
        // Parsear los días
        JsonNode daysNode = routineNode.get("days");
        List<RoutineDayDto> days = new ArrayList<>();
        
        for (JsonNode dayNode : daysNode) {
            RoutineDayDto dayDto = new RoutineDayDto();
            dayDto.setDayNumber(dayNode.get("dayNumber").asInt());
            dayDto.setDayName(dayNode.get("dayName").asText());
            dayDto.setDescription(dayNode.get("description").asText());
            
            // Parsear los ejercicios del día
            JsonNode exercisesNode = dayNode.get("exercises");
            List<RoutineExerciseDto> exercises = new ArrayList<>();
            int orderIndex = 0;
            
            for (JsonNode exerciseNode : exercisesNode) {
                RoutineExerciseDto exerciseDto = new RoutineExerciseDto();
                exerciseDto.setExerciseName(exerciseNode.get("exerciseName").asText());
                exerciseDto.setSets(exerciseNode.get("sets").asInt());
                exerciseDto.setReps(exerciseNode.get("reps").asText());
                exerciseDto.setRestSeconds(exerciseNode.get("restSeconds").asInt());
                exerciseDto.setNotes(exerciseNode.get("notes").asText());
                exerciseDto.setOrderIndex(orderIndex++);
                
                exercises.add(exerciseDto);
            }
            
            dayDto.setExercises(exercises);
            days.add(dayDto);
        }
        
        responseDto.setDays(days);
        
        return responseDto;
        
    } catch (Exception e) {
        throw new RuntimeException("Error al parsear el JSON de Ronnie: " + e.getMessage(), e);
    }
}
}
