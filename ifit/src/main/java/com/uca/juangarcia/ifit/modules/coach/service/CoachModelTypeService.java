package com.uca.juangarcia.ifit.modules.coach.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uca.juangarcia.ifit.exception.CoachModelTypeNotFoundException;
import com.uca.juangarcia.ifit.modules.coach.dto.CoachModelTypeResponseDto;
import com.uca.juangarcia.ifit.modules.coach.dto.CreateCoachModelTypeRequestDto;
import com.uca.juangarcia.ifit.modules.coach.dto.UpdateCoachModelTypeRequestDto;
import com.uca.juangarcia.ifit.modules.coach.mapper.CoachModelTypeMapper;
import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;
import com.uca.juangarcia.ifit.modules.coach.repository.CoachModelTypeRepository;

/**
 * Servicio para la gestión de tipos de modelos de coach de IA.
 * 
 * <p>Este servicio proporciona operaciones de negocio para gestionar los diferentes
 * tipos de modelos de IA disponibles en la aplicación, incluyendo:
 * <ul>
 *   <li>Creación de nuevos tipos de modelo</li>
 *   <li>Consulta de modelos disponibles</li>
 *   <li>Actualización de información de modelos</li>
 *   <li>Habilitación/deshabilitación de modelos</li>
 * </ul>
 * 
 * <p>Todas las operaciones de escritura se ejecutan dentro de transacciones
 * para garantizar la integridad de los datos.
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Service
public class CoachModelTypeService {
    
    private static final Logger logger = LoggerFactory.getLogger(CoachModelTypeService.class);
    
    private final CoachModelTypeRepository repository;
    private final CoachModelTypeMapper mapper;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param repository repositorio de datos de tipos de modelo de coach
     * @param mapper mapper para conversión entre entidades y DTOs
     */
    public CoachModelTypeService(CoachModelTypeRepository repository, 
                                 CoachModelTypeMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Obtiene todos los tipos de modelo de coach que están habilitados.
     * 
     * <p>Este método retorna únicamente los modelos que están marcados como
     * habilitados (enabled = true), los cuales pueden ser asignados a usuarios.
     * 
     * @return lista de DTOs con información de los modelos habilitados
     */
    @Transactional(readOnly = true)
    public List<CoachModelTypeResponseDto> getAllEnabled() {
        logger.debug("Fetching all enabled coach model types");
        
        List<CoachModelType> models = repository.findByEnabledTrue();
        
        logger.info("Found {} enabled coach model types", models.size());
        
        return models.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los tipos de modelo de coach del sistema.
     * 
     * <p>Este método retorna todos los modelos, incluyendo los deshabilitados.
     * Generalmente se usa en contextos administrativos.
     * 
     * @return lista de DTOs con información de todos los modelos
     */
    @Transactional(readOnly = true)
    public List<CoachModelTypeResponseDto> getAll() {
        logger.debug("Fetching all coach model types");
        
        List<CoachModelType> models = repository.findAll();
        
        logger.info("Found {} total coach model types", models.size());
        
        return models.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un tipo de modelo de coach por su ID.
     * 
     * @param id identificador único del tipo de modelo
     * @return DTO con la información del modelo
     * @throws CoachModelTypeNotFoundException si no existe un modelo con el ID proporcionado
     */
    @Transactional(readOnly = true)
    public CoachModelTypeResponseDto getById(Long id) throws CoachModelTypeNotFoundException {
        logger.debug("Fetching coach model type with id: {}", id);
        
        CoachModelType coachModelType = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Coach model type not found with id: {}", id);
                    return new CoachModelTypeNotFoundException(
                            "Coach model type not found with id: " + id);
                });
        
        logger.info("Coach model type found: {}", coachModelType.getName());
        
        return mapper.toResponseDto(coachModelType);
    }

    /**
     * Busca un tipo de modelo de coach por su nombre.
     * 
     * <p>La búsqueda es exacta y case-sensitive.
     * 
     * @param name nombre del modelo a buscar
     * @return DTO con la información del modelo
     * @throws CoachModelTypeNotFoundException si no existe un modelo con ese nombre
     */
    @Transactional(readOnly = true)
    public CoachModelTypeResponseDto getByName(String name) throws CoachModelTypeNotFoundException{
        logger.debug("Fetching coach model type with name: {}", name);
        
        CoachModelType coachModelType = repository.findByName(name)
                .orElseThrow(() -> {
                    logger.error("Coach model type not found with name: {}", name);
                    return new CoachModelTypeNotFoundException(
                            "Coach model type not found with name: " + name);
                });
        
        logger.info("Coach model type found: {}", coachModelType.getName());
        
        return mapper.toResponseDto(coachModelType);
    }

    /**
     * Crea un nuevo tipo de modelo de coach.
     * 
     * <p>Antes de crear el modelo, se valida que no exista otro con el mismo nombre.
     * El nombre debe ser único en el sistema.
     * 
     * @param dto datos del nuevo tipo de modelo a crear
     * @return DTO con la información del modelo creado
     * @throws IllegalArgumentException si ya existe un modelo con el mismo nombre
     */
    @Transactional
    public CoachModelTypeResponseDto create(CreateCoachModelTypeRequestDto dto) {
        logger.debug("Creating new coach model type with name: {}", dto.name());
        
        // Validar que el nombre no exista
        if (repository.findByName(dto.name()).isPresent()) {
            logger.error("Coach model type with name '{}' already exists", dto.name());
            throw new IllegalArgumentException(
                    "Coach model type with name '" + dto.name() + "' already exists");
        }
        
        // Convertir DTO a entidad
        CoachModelType coachModelType = mapper.toEntity(dto);
        
        // Guardar en base de datos
        CoachModelType saved = repository.save(coachModelType);
        
        logger.info("Coach model type created successfully with id: {} and name: {}", 
                saved.getId(), saved.getName());
        
        return mapper.toResponseDto(saved);
    }

    /**
     * Actualiza un tipo de modelo de coach existente.
     * 
     * <p>Permite actualización parcial de campos. Solo se modifican los campos
     * que no son nulos en el DTO de actualización.
     * 
     * <p>Si se intenta cambiar el nombre y el nuevo nombre ya está en uso,
     * se lanza una excepción.
     * 
     * @param id identificador del modelo a actualizar
     * @param dto datos a actualizar (campos opcionales)
     * @return DTO con la información del modelo actualizado
     * @throws CoachModelTypeNotFoundException si no existe un modelo con el ID proporcionado
     * @throws IllegalArgumentException si el nuevo nombre ya está en uso por otro modelo
     */
    @Transactional
    public CoachModelTypeResponseDto update(Long id, UpdateCoachModelTypeRequestDto dto) throws CoachModelTypeNotFoundException{
        logger.debug("Updating coach model type with id: {}", id);
        
        // Buscar el modelo existente
        CoachModelType coachModelType = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Coach model type not found with id: {}", id);
                    return new CoachModelTypeNotFoundException(
                            "Coach model type not found with id: " + id);
                });
        
        // Si se está cambiando el nombre, validar que no exista
        if (dto.name() != null && !dto.name().equals(coachModelType.getName())) {
            repository.findByName(dto.name()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    logger.error("Coach model type with name '{}' already exists", dto.name());
                    throw new IllegalArgumentException(
                            "Coach model type with name '" + dto.name() + "' already exists");
                }
            });
        }
        
        // Actualizar campos desde el DTO
        mapper.updateEntityFromDto(coachModelType, dto);
        
        // Guardar cambios
        CoachModelType updated = repository.save(coachModelType);
        
        logger.info("Coach model type updated successfully: {}", updated.getName());
        
        return mapper.toResponseDto(updated);
    }

    /**
     * Deshabilita un tipo de modelo de coach.
     * 
     * <p>Este método realiza un "soft delete", marcando el modelo como
     * deshabilitado (enabled = false) en lugar de eliminarlo físicamente.
     * Esto permite mantener la integridad referencial con usuarios que
     * tienen este modelo asignado.
     * 
     * <p>Los modelos deshabilitados no aparecen en las listas de modelos
     * disponibles ni pueden ser asignados a nuevos usuarios, pero siguen
     * funcionando para usuarios que ya lo tienen asignado.
     * 
     * @param id identificador del modelo a deshabilitar
     * @throws CoachModelTypeNotFoundException si no existe un modelo con el ID proporcionado
     */
    @Transactional
    public void delete(Long id) throws CoachModelTypeNotFoundException{
        logger.debug("Disabling coach model type with id: {}", id);
        
        CoachModelType coachModelType = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Coach model type not found with id: {}", id);
                    return new CoachModelTypeNotFoundException(
                            "Coach model type not found with id: " + id);
                });
        
        // Soft delete: marcar como deshabilitado
        coachModelType.setEnabled(false);
        repository.save(coachModelType);
        
        logger.info("Coach model type disabled successfully: {}", coachModelType.getName());
    }

    /**
     * Habilita un tipo de modelo de coach previamente deshabilitado.
     * 
     * <p>Este método permite reactivar modelos que fueron deshabilitados,
     * haciéndolos disponibles nuevamente para asignación a usuarios.
     * 
     * @param id identificador del modelo a habilitar
     * @return DTO con la información del modelo habilitado
     * @throws CoachModelTypeNotFoundException si no existe un modelo con el ID proporcionado
     */
    @Transactional
    public CoachModelTypeResponseDto enable(Long id) throws CoachModelTypeNotFoundException{
        logger.debug("Enabling coach model type with id: {}", id);
        
        CoachModelType coachModelType = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Coach model type not found with id: {}", id);
                    return new CoachModelTypeNotFoundException(
                            "Coach model type not found with id: " + id);
                });
        
        // Habilitar el modelo
        coachModelType.setEnabled(true);
        CoachModelType enabled = repository.save(coachModelType);
        
        logger.info("Coach model type enabled successfully: {}", enabled.getName());
        
        return mapper.toResponseDto(enabled);
    }

    /**
     * Verifica si existe un tipo de modelo con el ID especificado.
     * 
     * @param id identificador del modelo a verificar
     * @return true si existe, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    /**
     * Cuenta el número total de tipos de modelo en el sistema.
     * 
     * @return número total de modelos (habilitados y deshabilitados)
     */
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    /**
     * Cuenta el número de tipos de modelo habilitados.
     * 
     * @return número de modelos habilitados
     */
    @Transactional(readOnly = true)
    public long countEnabled() {
        return repository.findByEnabledTrue().size();
    }
}