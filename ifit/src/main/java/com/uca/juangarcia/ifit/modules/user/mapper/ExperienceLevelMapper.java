package com.uca.juangarcia.ifit.modules.user.mapper;

import org.springframework.stereotype.Component;

import com.uca.juangarcia.ifit.modules.user.dto.CreateExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.dto.ExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;

/**
 * Mapper para convertir entre ExperienceLevel y sus DTOs.
 * 
 * @author JuanGC
 * @version 1.0
 * @since 1.0
 */
@Component
public class ExperienceLevelMapper {
    
    /**
     * Convierte una entidad ExperienceLevel a un DTO ExperienceLevelDto
     * 
     * @param dto. ExperienceLevelDto a convertir
     * @return ExperienceLevel. Entidad convertida
     */
    public ExperienceLevel toEntity(ExperienceLevelDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("ExperienceLevelDto cannot be null");
        }

        ExperienceLevel experienceLevel = new ExperienceLevel();
        experienceLevel.setId(dto.getId());
        experienceLevel.setName(dto.getName());
        experienceLevel.setDescription(dto.getDescription());

        return experienceLevel;
    }

    /**
     * Convierte un DTO CreateExperienceLevelDto a una entidad ExperienceLevel
     * 
     * @param dto
     * @return
     */
    public ExperienceLevel toEntity(CreateExperienceLevelDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateExperienceLevelDto cannot be null");
        }

        ExperienceLevel experienceLevel = new ExperienceLevel();
        experienceLevel.setName(dto.name());
        experienceLevel.setDescription(dto.description());

        return experienceLevel;
    }

    /**
     * Convierte una entidad ExperienceLevel a un DTO ExperienceLevelDto
     * 
     * @param experienceLevel. Entidad a convertir
     * @return ExperienceLevelDto. DTO convertido
     */
    public ExperienceLevelDto toDto(ExperienceLevel experienceLevel) {
        if (experienceLevel == null) {
            throw new IllegalArgumentException("ExperienceLevel cannot be null");
        }

        ExperienceLevelDto dto = new ExperienceLevelDto();
        dto.setId(experienceLevel.getId());
        dto.setName(experienceLevel.getName());
        dto.setDescription(experienceLevel.getDescription());

        return dto;
    }
}