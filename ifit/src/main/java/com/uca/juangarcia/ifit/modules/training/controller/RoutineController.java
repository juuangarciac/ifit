package com.uca.juangarcia.ifit.modules.training.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uca.juangarcia.ifit.exception.UserIdNotFoundException;
import com.uca.juangarcia.ifit.exception.model.ErrorResponse;
import com.uca.juangarcia.ifit.modules.training.controller.dto.CreateRoutineRequestDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.GenerateRoutineRequestDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineDayDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineResponseDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.UpdateRoutineRequestDto;
import com.uca.juangarcia.ifit.exception.RoutineNotFoundException;
import com.uca.juangarcia.ifit.modules.training.service.RoutineService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de rutinas de entrenamiento.
 * 
 * Este controlador expone endpoints para realizar operaciones CRUD sobre rutinas,
 * así como funcionalidades específicas de gestión de rutinas de usuarios.
 * 
 * Todos los endpoints están bajo la ruta base {@code /api/v1/routines} siguiendo
 * las convenciones RESTful y versionado de API.
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/routines")
@Tag(name = "Routines", description = "API para gestión de rutinas de entrenamiento")
public class RoutineController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoutineService.class);
    
    private final RoutineService routineService;
    
    /**
     * Constructor con inyección de dependencias.
     * 
     * @param routineService servicio de rutinas
     */
    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }
    
    /**
     * Crea una nueva rutina de entrenamiento.
     * 
     * Endpoint: {@code POST /api/v1/routines}
     * 
     * @param requestDto datos de la rutina a crear
     * @return ResponseEntity con la rutina creada y código 201 CREATED
     * @throws UserIdNotFoundException si el usuario no existe
     */
    @PostMapping
    @Operation(
        summary = "Crear rutina",
        description = "Crea una nueva rutina de entrenamiento para un usuario, incluyendo días y ejercicios"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Rutina creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutineResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<RoutineResponseDto> createRoutine(
            @Parameter(description = "Datos de la rutina a crear", required = true)
            @Valid @RequestBody CreateRoutineRequestDto requestDto
    ) throws UserIdNotFoundException {
        logger.debug("Received create routine request: {}", requestDto);
        RoutineResponseDto createdRoutine = routineService.createRoutine(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoutine);
    }
    
    /**
     * Obtiene todas las rutinas del sistema.
     * 
     * Endpoint: {@code GET /api/v1/routines}
     * 
     * Para grandes conjuntos de datos, se recomienda usar
     * el endpoint paginado {@code GET /api/v1/routines/paginated}.
     * 
     * @return ResponseEntity con la lista de rutinas y código 200 OK
     */
    @GetMapping
    @Operation(
        summary = "Obtener todas las rutinas",
        description = "Retorna una lista completa de todas las rutinas del sistema. " +
                     "Para grandes volúmenes, se recomienda usar el endpoint paginado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de rutinas obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutineResponseDto.class))
        )
    })
    public ResponseEntity<List<RoutineResponseDto>> getAllRoutines() {
        List<RoutineResponseDto> routines = routineService.findAllRoutines();
        return ResponseEntity.ok(routines);
    }
    
    /**
     * Obtiene rutinas con paginación.
     * 
     * Endpoint: {@code GET /api/v1/routines/paginated}
     * 
     * Ejemplo de uso:
     * GET /api/v1/routines/paginated?page=0&size=10&sort=createdAt,desc
     * 
     * @param page número de página (comienza en 0)
     * @param size tamaño de página (elementos por página)
     * @param sortBy campo por el cual ordenar (por defecto: createdAt)
     * @param sortDir dirección de ordenamiento: asc o desc (por defecto: desc)
     * @return ResponseEntity con la página de rutinas y código 200 OK
     */
    @GetMapping("/paginated")
    @Operation(
        summary = "Obtener rutinas paginadas",
        description = "Retorna una página de rutinas con soporte para ordenamiento"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Página de rutinas obtenida exitosamente"
        )
    })
    public ResponseEntity<Page<RoutineResponseDto>> getRoutinesPaginated(
            @Parameter(description = "Número de página (empieza en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Campo de ordenamiento", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Dirección de ordenamiento (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<RoutineResponseDto> routinesPage = routineService.findAllRoutines(pageable);
        return ResponseEntity.ok(routinesPage);
    }
    
    /**
     * Obtiene una rutina por su ID.
     * 
     * Endpoint: {@code GET /api/v1/routines/{id}}
     * 
     * @param id identificador único de la rutina
     * @return ResponseEntity con la rutina y código 200 OK
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener rutina por ID",
        description = "Retorna los datos completos de una rutina específica, incluyendo días y ejercicios"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rutina encontrada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutineResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Rutina no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<RoutineResponseDto> getRoutineById(
            @Parameter(description = "ID de la rutina", required = true, example = "1")
            @PathVariable Long id
    ) throws RoutineNotFoundException {
        RoutineResponseDto routine = routineService.findRoutineById(id);
        return ResponseEntity.ok(routine);
    }
    
    /**
     * Obtiene todas las rutinas de un usuario.
     * 
     * Endpoint: {@code GET /api/v1/routines/user/{userId}}
     * 
     * @param userId identificador del usuario
     * @return ResponseEntity con la lista de rutinas del usuario y código 200 OK
     * @throws UserIdNotFoundException si el usuario no existe
     */
    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Obtener rutinas por usuario",
        description = "Retorna todas las rutinas de un usuario específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rutinas del usuario obtenidas exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutineResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<RoutineResponseDto>> getRoutinesByUserId(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long userId
    ) throws UserIdNotFoundException {
        List<RoutineResponseDto> routines = routineService.findRoutinesByUserId(userId);
        return ResponseEntity.ok(routines);
    }
    
    /**
     * Obtiene rutinas de un usuario con paginación.
     * 
     * Endpoint: {@code GET /api/v1/routines/user/{userId}/paginated}
     * 
     * @param userId identificador del usuario
     * @param page número de página
     * @param size tamaño de página
     * @param sortBy campo para ordenar
     * @param sortDir dirección de ordenamiento
     * @return ResponseEntity con la página de rutinas y código 200 OK
     * @throws UserIdNotFoundException si el usuario no existe
     */
    @GetMapping("/user/{userId}/paginated")
    @Operation(
        summary = "Obtener rutinas de usuario paginadas",
        description = "Retorna una página de rutinas de un usuario específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Página de rutinas obtenida exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public ResponseEntity<Page<RoutineResponseDto>> getRoutinesByUserIdPaginated(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long userId,
            
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) throws UserIdNotFoundException {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<RoutineResponseDto> routinesPage = routineService.findRoutinesByUserId(userId, pageable);
        return ResponseEntity.ok(routinesPage);
    }
    
    /**
     * Obtiene las rutinas activas de un usuario.
     * 
     * Endpoint: {@code GET /api/v1/routines/user/{userId}/active}
     * 
     * @param userId identificador del usuario
     * @return ResponseEntity con la lista de rutinas activas y código 200 OK
     * @throws UserIdNotFoundException si el usuario no existe
     */
    @GetMapping("/user/{userId}/active")
    @Operation(
        summary = "Obtener rutinas activas de usuario",
        description = "Retorna solo las rutinas activas de un usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rutinas activas obtenidas exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public ResponseEntity<List<RoutineResponseDto>> getActiveRoutinesByUserId(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long userId
    ) throws UserIdNotFoundException {
        List<RoutineResponseDto> routines = routineService.findActiveRoutinesByUserId(userId);
        return ResponseEntity.ok(routines);
    }
    
    /**
     * Actualiza una rutina existente.
     * 
     * Endpoint: {@code PUT /api/v1/routines/{id}}
     * 
     * Soporta actualizaciones parciales. Los campos no incluidos en el request
     * no se modificarán.
     * 
     * @param id identificador de la rutina a actualizar
     * @param updateDto datos a actualizar
     * @return ResponseEntity con la rutina actualizada y código 200 OK
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar rutina",
        description = "Actualiza los datos de una rutina existente. Soporta actualizaciones parciales."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rutina actualizada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutineResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Rutina no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<RoutineResponseDto> updateRoutine(
            @Parameter(description = "ID de la rutina a actualizar", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "Datos a actualizar", required = true)
            @Valid @RequestBody UpdateRoutineRequestDto updateDto
    ) throws RoutineNotFoundException {
        RoutineResponseDto updatedRoutine = routineService.updateRoutine(id, updateDto);
        return ResponseEntity.ok(updatedRoutine);
    }
    
    /**
     * Activa o desactiva una rutina.
     * 
     * Endpoint: {@code PATCH /api/v1/routines/{id}/toggle-active}
     * 
     * @param id identificador de la rutina
     * @param isActive true para activar, false para desactivar
     * @return ResponseEntity con la rutina actualizada y código 200 OK
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @PatchMapping("/{id}/toggle-active")
    @Operation(
        summary = "Activar/desactivar rutina",
        description = "Cambia el estado activo de una rutina"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estado de rutina actualizado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Rutina no encontrada"
        )
    })
    public ResponseEntity<RoutineResponseDto> toggleRoutineActive(
            @Parameter(description = "ID de la rutina", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "Estado activo (true/false)", required = true)
            @RequestParam boolean isActive
    ) throws RoutineNotFoundException {
        RoutineResponseDto updatedRoutine = routineService.toggleRoutineActive(id, isActive);
        return ResponseEntity.ok(updatedRoutine);
    }
    
    /**
     * Elimina una rutina del sistema.
     * 
     * Endpoint: {@code DELETE /api/v1/routines/{id}}
     * 
     * Advertencia: Esta operación es irreversible y eliminará también
     * todos los días y ejercicios asociados.
     * 
     * @param id identificador de la rutina a eliminar
     * @return ResponseEntity con código 204 NO CONTENT
     * @throws RoutineNotFoundException si no existe la rutina
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar rutina",
        description = "Elimina permanentemente una rutina del sistema, incluyendo días y ejercicios. " +
                     "Esta operación es irreversible."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Rutina eliminada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Rutina no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Void> deleteRoutine(
            @Parameter(description = "ID de la rutina a eliminar", required = true)
            @PathVariable Long id
    ) throws RoutineNotFoundException {
        routineService.deleteRoutine(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Cuenta las rutinas activas de un usuario.
     * 
     * Endpoint: {@code GET /api/v1/routines/user/{userId}/count-active}
     * 
     * @param userId identificador del usuario
     * @return ResponseEntity con el número de rutinas activas y código 200 OK
     */
    @GetMapping("/user/{userId}/count-active")
    @Operation(
        summary = "Contar rutinas activas",
        description = "Retorna el número de rutinas activas de un usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Conteo realizado exitosamente"
        )
    })
    public ResponseEntity<Long> countActiveRoutines(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long userId
    ) {
        long count = routineService.countActiveRoutinesByUserId(userId);
        return ResponseEntity.ok(count);
    }

    @Operation(
        summary = "Generar rutina personalizada",
        description = "Genera una rutina de entrenamiento personalizada basada en las respuestas del cuestionario completado. " +
                     "El backend construye automáticamente el prompt con la información del usuario y llama al servicio de IA (Ronnie) " +
                     "para generar una rutina estructurada con ejercicios, series, repeticiones y recomendaciones personalizadas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rutina generada exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "message": "¡Vamos a hacer que tú también seas un campeón! Este plan de entrenamiento es para principiantes...",
                        "routine": {
                            "userId": "user123",
                            "description": "Rutina Push-Pull-Legs para ganar masa muscular en casa...",
                            "trainingDays": 3,
                            "days": [
                                {
                                    "dayNumber": 1,
                                    "dayName": "Día 1 - Tren Push",
                                    "description": "Trabajamos en pecho, hombros y tríceps...",
                                    "exercises": [
                                        {
                                            "exerciseName": "Flexiones de pecho",
                                            "sets": 3,
                                            "reps": "8-12",
                                            "restSeconds": 90,
                                            "notes": "Mantén la espalda recta y el core activado."
                                        }
                                    ]
                                }
                            ]
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos o cuestionario no completado"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario o respuesta de cuestionario no encontrados"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor o error en el servicio de IA"
        )
    })
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoutineResponseDto> generateRoutine(
            @Parameter(
                description = "Objeto con userId y responseId para generar la rutina personalizada",
                required = true,
                content = @Content(schema = @Schema(implementation = GenerateRoutineRequestDto.class))
            )
            @Valid @RequestBody GenerateRoutineRequestDto request) throws UserIdNotFoundException {
                
            // Devolver el JSON directamente
            return ResponseEntity.ok(routineService.generateRoutine(
                    request.getUserId(),
                    request.getResponseId(),
                    request.getCoachType(),
                    request.getNote()
                ));
    }

    @Operation(
        summary = "Obtener día específico de una rutina",
        description = "Retorna los detalles de un día específico de una rutina, incluyendo los ejercicios programados para ese día. " +
                     "Útil para mostrar la información detallada del día seleccionado en la interfaz de usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Día de rutina obtenido exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutineDayDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Día de rutina no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @GetMapping("/{routineId}/day/{day}")
    public ResponseEntity<RoutineDayDto> getRoutineDayByRoutineIdAndDay(
            @Parameter(description = "ID de la rutina", required = true)
            @PathVariable Long routineId,
            
            @Parameter(description = "Número del día (1-7)", required = true)
            @PathVariable Integer day
    ) throws RoutineNotFoundException {
        RoutineDayDto routineDay = routineService.getRoutineDayByRoutineIdAndDay(routineId, day);
        return ResponseEntity.ok(routineDay);
    }


    @Operation(
        summary = "Marcar día de rutina como completado",
        description = "Marca un día específico de una rutina como completado. Esto puede ser utilizado para llevar un seguimiento del progreso del usuario y mostrar visualmente qué días han sido completados en la interfaz de usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Día de rutina marcado como completado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutineResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Día de rutina no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/{routineId}/day/{day}/complete")
    public ResponseEntity<RoutineResponseDto> setRoutineDayAsCompleted(
        @Parameter(description = "ID de la rutina", required = true)
        @PathVariable
        Long routineId, 
        @Parameter(description = "Número del día a marcar como completado (1-7)", required = true)
        @PathVariable
        Integer day) 
    throws RoutineNotFoundException {
        RoutineResponseDto updatedRoutine = routineService.setRoutineDayAsCompleted(routineId, day);
        return ResponseEntity.ok(updatedRoutine);
    }



    @Operation(
        summary = "Marcar una rutina como completada",
        description = "Marca toda la rutina como completada."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Rutina marcada como completada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RoutineResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Rutina no encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/{routineId}/complete")
    public ResponseEntity<RoutineResponseDto> setRoutineAsCompleted(
        @Parameter(description = "ID de la rutina", required = true)
        @PathVariable Long routineId)
    throws RoutineNotFoundException {
        RoutineResponseDto updatedRoutine = routineService.setRoutineAsCompleted(routineId);

        return ResponseEntity.ok(updatedRoutine);

    }

}
