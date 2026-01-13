package com.uca.juangarcia.ifit.modules.coach.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uca.juangarcia.ifit.modules.coach.dto.CoachModelTypeResponseDto;
import com.uca.juangarcia.ifit.modules.coach.dto.CreateCoachModelTypeRequestDto;
import com.uca.juangarcia.ifit.modules.coach.dto.UpdateCoachModelTypeRequestDto;
import com.uca.juangarcia.ifit.modules.coach.service.CoachModelTypeService;
import com.uca.juangarcia.ifit.shared.exception.CoachModelTypeNotFoundException;
import com.uca.juangarcia.ifit.shared.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de tipos de modelos de coach.
 * 
 * <p>Este controlador expone endpoints para realizar operaciones CRUD sobre
 * los diferentes tipos de modelos de IA disponibles para coaching personalizado.
 * Los modelos pueden ser habilitados o deshabilitados según disponibilidad.
 * 
 * <p>Todos los endpoints están bajo la ruta base {@code /coach-models} siguiendo
 * las convenciones RESTful y versionado de API.
 * 
 * <p><strong>Seguridad:</strong>
 * <ul>
 *   <li>GET endpoints: Accesibles para usuarios autenticados</li>
 *   <li>POST/PUT/DELETE endpoints: Solo administradores (ROLE_ADMIN)</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/coach-models")
@Tag(name = "Coach Models", description = "Gestión de tipos de modelo de coach de IA")
public class CoachModelTypeController {
    
    private final CoachModelTypeService service;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param service servicio de lógica de negocio para tipos de modelo de coach
     */
    public CoachModelTypeController(CoachModelTypeService service) {
        this.service = service;
    }

    /**
     * Obtiene todos los tipos de modelo de coach habilitados.
     * 
     * <p><strong>Endpoint:</strong> {@code GET /ifit/api/v1/coach-models}
     * 
     * <p>Este endpoint retorna únicamente los modelos que están actualmente
     * habilitados y disponibles para ser asignados a usuarios. Los modelos
     * deshabilitados no aparecen en esta lista.
     * 
     * @return ResponseEntity con lista de modelos habilitados
     */
    @GetMapping
    @Operation(
        summary = "Listar modelos habilitados", 
        description = "Obtiene todos los tipos de modelo de coach de IA que están actualmente disponibles y habilitados en el sistema"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de modelos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CoachModelTypeResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado - Token JWT inválido o ausente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<CoachModelTypeResponseDto>> getAllEnabled() {
        List<CoachModelTypeResponseDto> models = service.getAllEnabled();
        return ResponseEntity.ok(models);
    }

    /**
     * Obtiene todos los tipos de modelo de coach (habilitados y deshabilitados).
     * 
     * <p><strong>Endpoint:</strong> {@code GET /ifit/api/v1/coach-models/all}
     * 
     * <p><strong>Nota:</strong> Este endpoint está restringido a administradores
     * ya que expone información de modelos deshabilitados.
     * 
     * @return ResponseEntity con lista de todos los modelos
     */
    @GetMapping("/all")
    @Operation(
        summary = "Listar todos los modelos", 
        description = "Obtiene todos los tipos de modelo de coach, incluyendo los deshabilitados. Requiere rol de administrador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Lista completa de modelos obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CoachModelTypeResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Requiere rol ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<CoachModelTypeResponseDto>> getAll() {
        List<CoachModelTypeResponseDto> models = service.getAll();
        return ResponseEntity.ok(models);
    }

    /**
     * Obtiene un tipo de modelo de coach específico por su ID.
     * 
     * <p><strong>Endpoint:</strong> {@code GET /ifit/api/v1/coach-models/{id}}
     * 
     * @param id identificador único del tipo de modelo
     * @return ResponseEntity con los datos del modelo
     * @throws CoachModelTypeNotFoundException si no existe un modelo con el ID proporcionado
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener modelo por ID",
        description = "Obtiene la información detallada de un tipo de modelo de coach específico"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Modelo encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CoachModelTypeResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Modelo no encontrado con el ID proporcionado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<CoachModelTypeResponseDto> getById(@PathVariable Long id) throws CoachModelTypeNotFoundException {
        CoachModelTypeResponseDto model = service.getById(id);
        return ResponseEntity.ok(model);
    }

    /**
     * Busca un tipo de modelo de coach por su nombre.
     * 
     * <p><strong>Endpoint:</strong> {@code GET /ifit/api/v1/coach-models/name/{name}}
     * 
     * <p>El nombre debe coincidir exactamente (case-sensitive).
     * 
     * @param name nombre del modelo a buscar
     * @return ResponseEntity con los datos del modelo
     * @throws CoachModelTypeNotFoundException si no existe un modelo con ese nombre
     */
    @GetMapping("/name/{name}")
    @Operation(
        summary = "Buscar modelo por nombre",
        description = "Busca un tipo de modelo de coach por su nombre exacto"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Modelo encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CoachModelTypeResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "No se encontró ningún modelo con ese nombre",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<CoachModelTypeResponseDto> getByName(@PathVariable String name) throws CoachModelTypeNotFoundException{
        CoachModelTypeResponseDto model = service.getByName(name);
        return ResponseEntity.ok(model);
    }

    /**
     * Crea un nuevo tipo de modelo de coach.
     * 
     * <p><strong>Endpoint:</strong> {@code POST /ifit/api/v1/coach-models}
     * 
     * <p><strong>Restricción:</strong> Solo administradores (ROLE_ADMIN)
     * 
     * <p>El nombre del modelo debe ser único en el sistema. Si ya existe
     * un modelo con el mismo nombre, se retornará un error 400.
     * 
     * @param dto datos del nuevo tipo de modelo a crear
     * @return ResponseEntity con el modelo creado y código 201 (Created)
     */
    @PostMapping
    @Operation(
        summary = "Crear nuevo modelo",
        description = "Crea un nuevo tipo de modelo de coach de IA. Requiere rol de administrador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201", 
            description = "Modelo creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CoachModelTypeResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos o nombre de modelo duplicado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Requiere rol ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<CoachModelTypeResponseDto> create(
            @Valid @RequestBody CreateCoachModelTypeRequestDto dto) {
        CoachModelTypeResponseDto created = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un tipo de modelo de coach existente.
     * 
     * <p><strong>Endpoint:</strong> {@code PUT /ifit/api/v1/coach-models/{id}}
     * 
     * <p><strong>Restricción:</strong> Solo administradores (ROLE_ADMIN)
     * 
     * <p>Permite actualización parcial de campos. Solo se modifican los campos
     * que no son nulos en el DTO de actualización.
     * 
     * @param id identificador del modelo a actualizar
     * @param dto datos a actualizar (campos opcionales)
     * @return ResponseEntity con el modelo actualizado
     * @throws CoachModelTypeNotFoundException si no existe un modelo con el ID proporcionado
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar modelo",
        description = "Actualiza un tipo de modelo de coach existente. Permite actualización parcial. Requiere rol de administrador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Modelo actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CoachModelTypeResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Modelo no encontrado con el ID proporcionado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos inválidos en la actualización",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Requiere rol ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<CoachModelTypeResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCoachModelTypeRequestDto dto) throws CoachModelTypeNotFoundException{
        CoachModelTypeResponseDto updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deshabilita (soft delete) un tipo de modelo de coach.
     * 
     * <p><strong>Endpoint:</strong> {@code DELETE /ifit/api/v1/coach-models/{id}}
     * 
     * <p><strong>Restricción:</strong> Solo administradores (ROLE_ADMIN)
     * 
     * <p><strong>Nota importante:</strong> Este endpoint NO elimina el modelo
     * de la base de datos, sino que lo marca como deshabilitado (enabled = false).
     * Esto permite mantener la integridad referencial con usuarios que tienen
     * este modelo asignado.
     * 
     * <p>Los modelos deshabilitados:
     * <ul>
     *   <li>No aparecen en la lista de modelos disponibles</li>
     *   <li>No pueden ser asignados a nuevos usuarios</li>
     *   <li>Siguen funcionando para usuarios que ya lo tienen asignado</li>
     * </ul>
     * 
     * @param id identificador del modelo a deshabilitar
     * @return ResponseEntity vacío con código 204 (No Content)
     * @throws CoachModelTypeNotFoundException si no existe un modelo con el ID proporcionado
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Deshabilitar modelo",
        description = "Deshabilita un tipo de modelo de coach (soft delete). El modelo no se elimina, solo se marca como no disponible. Requiere rol de administrador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204", 
            description = "Modelo deshabilitado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Modelo no encontrado con el ID proporcionado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Requiere rol ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) throws CoachModelTypeNotFoundException{
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Habilita un tipo de modelo de coach previamente deshabilitado.
     * 
     * <p><strong>Endpoint:</strong> {@code PATCH /ifit/api/v1/coach-models/{id}/enable}
     * 
     * <p><strong>Restricción:</strong> Solo administradores (ROLE_ADMIN)
     * 
     * <p>Este endpoint permite reactivar modelos que fueron deshabilitados
     * anteriormente, haciéndolos disponibles nuevamente para asignación.
     * 
     * @param id identificador del modelo a habilitar
     * @return ResponseEntity con el modelo habilitado
     * @throws CoachModelTypeNotFoundException si no existe un modelo con el ID proporcionado
     */
    @PutMapping("/{id}/enable")
    @Operation(
        summary = "Habilitar modelo",
        description = "Habilita un tipo de modelo de coach previamente deshabilitado. Requiere rol de administrador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Modelo habilitado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CoachModelTypeResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Modelo no encontrado con el ID proporcionado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado - Requiere rol ADMIN",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<CoachModelTypeResponseDto> enable(@PathVariable Long id) throws CoachModelTypeNotFoundException{
        CoachModelTypeResponseDto enabled = service.enable(id);
        return ResponseEntity.ok(enabled);
    }
}