package com.uca.juangarcia.ifit.modules.exercises.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uca.juangarcia.ifit.modules.exercises.dto.ExerciseDetailDto;
import com.uca.juangarcia.ifit.modules.exercises.dto.ExerciseSummaryDto;
import com.uca.juangarcia.ifit.modules.exercises.model.ExerciseCatalog;
import com.uca.juangarcia.ifit.modules.exercises.repository.ExerciseCatalogRepository;

/**
 * Servicio para consulta del catálogo de ejercicios disponibles.
 *
 * <p>Solo operaciones de lectura. El catálogo es un recurso estático
 * importado desde el CSV del microservicio Ronnie.
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Service
@Transactional(readOnly = true)
public class ExerciseCatalogService {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseCatalogService.class);

    // Prefijo base para las URLs de imagen (servidas por Ronnie vía gateway)
    private static final String IMAGE_BASE_URL = "/exercise-images/";

    private final ExerciseCatalogRepository repository;

    public ExerciseCatalogService(ExerciseCatalogRepository repository) {
        this.repository = repository;
    }

    /**
     * Devuelve una página del catálogo con filtros opcionales.
     *
     * @param level     nivel de dificultad (principiante, intermedio, avanzado) — opcional
     * @param category  categoría (fuerza, estiramiento, cardio…) — opcional
     * @param equipment equipamiento necesario — opcional
     * @param muscle    músculo principal (búsqueda parcial) — opcional
     * @param pageable  configuración de paginación y ordenación
     * @return página de resúmenes de ejercicios
     */
    public Page<ExerciseSummaryDto> getExercises(
            String level, String category, String equipment, String muscle, Pageable pageable) {

        logger.debug("Fetching exercises — level={}, category={}, equipment={}, muscle={}",
                level, category, equipment, muscle);

        Page<ExerciseCatalog> page = repository.findWithFilters(
                blankToNull(level),
                blankToNull(category),
                blankToNull(equipment),
                blankToNull(muscle),
                pageable);

        logger.info("Found {} exercises (page {}/{})",
                page.getTotalElements(), page.getNumber(), page.getTotalPages());

        return page.map(this::toSummaryDto);
    }

    /**
     * Devuelve el detalle completo de un ejercicio por su ID.
     *
     * @param id identificador del ejercicio
     * @return detalle con instrucciones e imágenes
     * @throws IllegalArgumentException si no existe el ejercicio
     */
    public ExerciseDetailDto getExerciseById(Long id) {
        logger.debug("Fetching exercise detail for id={}", id);

        ExerciseCatalog exercise = repository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Exercise not found with id={}", id);
                    return new IllegalArgumentException("Exercise not found with id: " + id);
                });

        logger.info("Exercise found: {}", exercise.getName());
        return toDetailDto(exercise);
    }

    // -------------------------------------------------------------------------
    // Mappers privados
    // -------------------------------------------------------------------------

    private ExerciseSummaryDto toSummaryDto(ExerciseCatalog e) {
        return new ExerciseSummaryDto(
                e.getId(),
                e.getName(),
                e.getLevel(),
                e.getCategory(),
                e.getEquipment(),
                e.getMechanic(),
                e.getPrimaryMuscles(),
                e.getSecondaryMuscles(),
                buildImageUrls(e.getImages()));
    }

    private ExerciseDetailDto toDetailDto(ExerciseCatalog e) {
        return new ExerciseDetailDto(
                e.getId(),
                e.getName(),
                e.getForce(),
                e.getLevel(),
                e.getMechanic(),
                e.getEquipment(),
                e.getCategory(),
                e.getCode(),
                e.getPrimaryMuscles(),
                e.getSecondaryMuscles(),
                e.getInstructions(),
                buildImageUrls(e.getImages()));
    }

    /**
     * Transforma rutas relativas como "Adductor/0.jpg"
     * en URLs navegables como "/exercise-images/Adductor/0.jpg".
     */
    private List<String> buildImageUrls(List<String> rawPaths) {
        if (rawPaths == null) return List.of();
        return rawPaths.stream()
                .map(path -> IMAGE_BASE_URL + path)
                .collect(Collectors.toList());
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
