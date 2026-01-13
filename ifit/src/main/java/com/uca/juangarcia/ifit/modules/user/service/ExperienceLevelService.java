package com.uca.juangarcia.ifit.modules.user.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.uca.juangarcia.ifit.modules.user.dto.CreateExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.dto.ExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.dto.UpdateExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.mapper.ExperienceLevelMapper;
import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;
import com.uca.juangarcia.ifit.modules.user.repository.ExperienceLevelRepository;
import com.uca.juangarcia.ifit.shared.exception.ExperienceLevelNotFoundException;

/**
 * Servicio para manejar los niveles de experiencia de los usuarios.
 * 
 * @author Juan Garcia
 * @version 1.0
 */
@Service
public class ExperienceLevelService {

    @Autowired
    private ExperienceLevelRepository experienceLevelRepository;

    @Autowired
    private ExperienceLevelMapper experienceLevelMapper;

    /**
     * Guarda un nivel de experiencia
     * 
     * @param experienceLevel. Nivel de experiencia a guardar
     * @return
     */
    public ExperienceLevelDto createExperienceLevel(CreateExperienceLevelDto experienceLevelDto){
        if(experienceLevelDto == null){
            throw new IllegalArgumentException("experienceLevel cannot be empty or null.");
        }

        if(experienceLevelRepository.findByName(experienceLevelDto.name()).isPresent()){
            throw new IllegalArgumentException("An experience level with the name " 
                + experienceLevelDto.name() + " already exists.");
        }

        ExperienceLevel experienceLevel = experienceLevelMapper.toEntity(experienceLevelDto);

        ExperienceLevel savedExperienceLevel = experienceLevelRepository.save(experienceLevel);

        return experienceLevelMapper.toDto(savedExperienceLevel);
    }

    /**
     * Actualiza un nivel de experiencia por su ID
     * 
     * @param experienceLevelId. ID del nivel de experiencia a actualizar
     * @param updateExperienceLevelDto. Datos para actualizar el nivel de experiencia
     * @return ExperienceLevelDto
     * @throws ExperienceLevelNotFoundException. Si no se encuentra el nivel de experiencia
     */
    public ExperienceLevelDto updateExperienceLevel(Long experienceLevelId, UpdateExperienceLevelDto updateExperienceLevelDto) throws ExperienceLevelNotFoundException {
        if(experienceLevelId == null){
            throw new IllegalArgumentException("experienceLevelId cannot be null or empty.");
        }

        if(updateExperienceLevelDto == null){
            throw new IllegalArgumentException("updateExperienceLevelDto cannot be null or empty.");
        }

        ExperienceLevel experienceLevel = experienceLevelRepository.findById(experienceLevelId)
                            .orElseThrow(() -> new ExperienceLevelNotFoundException(experienceLevelId));

        experienceLevel.setDescription(updateExperienceLevelDto.description());

        ExperienceLevel updatedExperience = experienceLevelRepository.save(experienceLevel);

        return experienceLevelMapper.toDto(updatedExperience);
    }

    /**
     * Elimina un nivel de experiencia por su ID
     * 
     * @param experienceLevelId. ID del nivel de experiencia a eliminar
     * @throws ExperienceLevelNotFoundException. Si no se encuentra el nivel de experiencia
     */
    public void deleteExperienceLevel(Long experienceLevelId) throws ExperienceLevelNotFoundException {
        if(experienceLevelId == null){
            throw new IllegalArgumentException("experienceLevelId cannot be null or empty.");
        }

        if(!experienceLevelRepository.existsById(experienceLevelId)){
            throw new ExperienceLevelNotFoundException(experienceLevelId);
        }

        experienceLevelRepository.deleteById(experienceLevelId);
    }

    /**
     * Obtiene todos los niveles de experiencia
     * 
     * @return List<ExperienceLevelDto>
     * @throws IllegalArgumentException. Si no hay niveles de experiencia disponibles
     */
    public List<ExperienceLevelDto> getAll(){
        List<ExperienceLevel> experienceLevels = experienceLevelRepository.findAll();

        Assert.isTrue(experienceLevels != null && !experienceLevels.isEmpty(), 
            "There are no experience levels available.");

        return experienceLevels.stream()
                    .map(ExperienceLevelDto::new)
                    .toList();
    }

    /**
     * Obtiene un nivel de experiencia por su ID
     * @param experienceLevelId
     * @return
     * @throws ExperienceLevelNotFoundException
     */
    public ExperienceLevelDto getById(Long experienceLevelId) throws ExperienceLevelNotFoundException
    {
        Assert.notNull(experienceLevelId, "ExperienceLevel ID cannot be null or empty.");

        ExperienceLevel experienceLevel = experienceLevelRepository.findById(experienceLevelId)
                            .orElseThrow(() -> new ExperienceLevelNotFoundException(experienceLevelId));

        return experienceLevelMapper.toDto(experienceLevel);
    }

    /**
     * Obtiene un nivel de experiencia por su nombre
     * 
     * @param name. Nombre del nivel de experiencia
     * @return ExperienceLevelDto
     * @throws ExperienceLevelNotFoundException. Si no se encuentra el nivel de experiencia
     */
    public ExperienceLevelDto getByName(String name) throws ExperienceLevelNotFoundException {
        Assert.hasText(name, "ExperienceLevel name cannot be null or empty.");

        ExperienceLevel experienceLevel = experienceLevelRepository.findByName(name)
                            .orElseThrow(() -> new ExperienceLevelNotFoundException(name));

        return experienceLevelMapper.toDto(experienceLevel);
    }
}