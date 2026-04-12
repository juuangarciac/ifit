package com.uca.juangarcia.ifit.modules.exercises.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uca.juangarcia.ifit.modules.exercises.dto.ExerciseDetailDto;
import com.uca.juangarcia.ifit.modules.exercises.dto.ExerciseSummaryDto;
import com.uca.juangarcia.ifit.modules.exercises.service.ExerciseCatalogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador REST para el catálogo de ejercicios disponibles.
 *
 * <p>Expone el catálogo estático de ejercicios importado desde Ronnie.
 * Las imágenes se sirven desde Ronnie a través del gateway:
 * {@code GET /ifit/api/v1/exercise-images/{carpeta}/{archivo}.jpg}
 *
 * @author Juan Garcia
 * @version 1.0
 */
@RestController
@RequestMapping("/exercises")
@Tag(name = "Exercise Catalog", description = "Catálogo de ejercicios disponibles con imágenes")
public class ExerciseCatalogController {

    private final ExerciseCatalogService service;

    public ExerciseCatalogController(ExerciseCatalogService service) {
        this.service = service;
    }

    /**
     * Lista paginada del catálogo con filtros opcionales.
     *
     * <p>Ejemplos de uso:
     * <ul>
     *   <li>{@code GET /exercises?page=0&size=20}</li>
     *   <li>{@code GET /exercises?level=principiante&category=fuerza}</li>
     *   <li>{@code GET /exercises?muscle=pecho&size=10}</li>
     * </ul>
     */
    @GetMapping
    @Operation(
        summary = "Listar catálogo de ejercicios",
        description = "Devuelve una página del catálogo con filtros opcionales por nivel, categoría, " +
                      "equipamiento y músculo principal. Las URLs de las imágenes apuntan al servicio " +
                      "Ronnie a través del gateway."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Catálogo obtenido exitosamente")
    })
    public ResponseEntity<Page<ExerciseSummaryDto>> getExercises(

            @Parameter(description = "Nivel: principiante, intermedio, avanzado")
            @RequestParam(required = false) String level,

            @Parameter(description = "Categoría: fuerza, estiramiento, cardio, pliometria…")
            @RequestParam(required = false) String category,

            @Parameter(description = "Equipamiento: solo_cuerpo, barra, mancuernas, maquina…")
            @RequestParam(required = false) String equipment,

            @Parameter(description = "Músculo principal (búsqueda parcial, ej: 'pecho')")
            @RequestParam(required = false) String muscle,

            @Parameter(description = "Número de página (empieza en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Campo de ordenación", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,

            @Parameter(description = "Dirección: asc / desc", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return ResponseEntity.ok(service.getExercises(level, category, equipment, muscle, pageable));
    }

    /**
     * Detalle completo de un ejercicio, incluyendo instrucciones e imágenes.
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener detalle de un ejercicio",
        description = "Devuelve el detalle completo del ejercicio: instrucciones paso a paso, " +
                      "músculos trabajados e imágenes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ejercicio encontrado"),
        @ApiResponse(responseCode = "400", description = "Ejercicio no encontrado")
    })
    public ResponseEntity<ExerciseDetailDto> getExerciseById(
            @Parameter(description = "ID del ejercicio", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(service.getExerciseById(id));
    }
}
