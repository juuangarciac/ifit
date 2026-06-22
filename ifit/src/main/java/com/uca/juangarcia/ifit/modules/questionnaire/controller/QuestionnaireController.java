package com.uca.juangarcia.ifit.modules.questionnaire.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uca.juangarcia.ifit.exception.CoachModelTypeNotFoundException;
import com.uca.juangarcia.ifit.exception.ExperienceLevelNotFoundException;
import com.uca.juangarcia.ifit.exception.QuestionNotFoundException;
import com.uca.juangarcia.ifit.exception.QuestionnaireNotFoundException;
import com.uca.juangarcia.ifit.exception.UserIdNotFoundException;
import com.uca.juangarcia.ifit.exception.model.ErrorResponse;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.AnswerRequestDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.CreateQuestionnaireRequestDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireResponseDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireResponseSummaryDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireSummaryDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireWithFirstQuestionDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.UpdateQuestionnaireRequestDto;
import com.uca.juangarcia.ifit.modules.questionnaire.service.QuestionnaireService;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


/**
 * Controlador REST para la gestión de cuestionarios.
 * 
 * <p>Proporciona dos grupos de endpoints:
 * <ul>
 *   <li><strong>CRUD de Cuestionarios: Operaciones sobre la plantilla del cuestionario</li>
 *   <li><strong>Sesiones de Usuario: Iniciar, responder y consultar respuestas de usuarios</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 3.0
 * @since 1.0
 */
@RestController
@RequestMapping("/questionnaires")
@Validated
@Tag(name = "Questionnaires", description = "API para gestión de cuestionarios y respuestas de usuarios")
public class QuestionnaireController {
    
    private final QuestionnaireService questionnaireService;

    public QuestionnaireController(QuestionnaireService questionnaireService) {
        this.questionnaireService = questionnaireService;
    }
    
    
    /**
     * Obtiene todos los cuestionarios habilitados.
     * 
     * Endpoint: {@code GET /ifit/api/v1/questionnaires}
     * 
     * @return Lista de cuestionarios en formato resumido
     */
    @PreAuthorize("hasRole('admin_client_role')")
    @GetMapping
    @Operation(
        summary = "Listar cuestionarios",
        description = "Obtiene todos los cuestionarios habilitados en formato compacto (ideal para listados)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de cuestionarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireSummaryDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<QuestionnaireSummaryDto>> getAllQuestionnaires() {
        List<QuestionnaireSummaryDto> questionnaires = questionnaireService.getAllQuestionnaires();
        return ResponseEntity.ok(questionnaires);
    }
    
    /**
     * Obtiene un cuestionario por su ID.
     * 
     * Endpoint: {@code GET /ifit/api/v1/questionnaires/{id}}
     * 
     * @param id ID del cuestionario
     * @return Cuestionario completo con todos sus detalles
     * @throws QuestionnaireNotFoundException si no existe el cuestionario
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener cuestionario por ID",
        description = "Recupera un cuestionario específico con toda su información."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cuestionario encontrado",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cuestionario no encontrado",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireDto> getQuestionnaireById(@PathVariable Long id) 
            throws QuestionnaireNotFoundException {
        QuestionnaireDto questionnaire = questionnaireService.getQuestionnaireById(id);
        return ResponseEntity.ok(questionnaire);
    }


    @GetMapping("/coach/{coachId}/experience-level/{experienceLevelId}")
    @Operation(
        summary = "Obtener cuestionario por coach y nivel de experiencia",
        description = "Recupera un cuestionario específico con toda su información."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cuestionario encontrado",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cuestionario no encontrado",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireDto> getQuestionnaireByCoachIdAndExperienceLevelId(
        @PathVariable Long coachId,
        @PathVariable Long experienceLevelId
    ) throws QuestionnaireNotFoundException {
        QuestionnaireDto response = questionnaireService.getQuestionnaireByCoachIdAndExperienceLevelId(coachId, experienceLevelId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene un cuestionario con su primera pregunta incluida.
     * Endpoint optimizado que combina la info del cuestionario + primera pregunta.
     * 
     * Endpoint: {@code GET /ifit/api/v1/questionnaires/{id}/with-first-question}
     * 
     * @param id ID del cuestionario
     * @return Cuestionario con primera pregunta y opciones
     * @throws QuestionnaireNotFoundException si no existe el cuestionario
     */
    @GetMapping("/{id}/with-first-question")
    @Operation(
        summary = "Obtener cuestionario con primera pregunta",
        description = "Obtiene el cuestionario junto con su primera pregunta y opciones. Útil para reducir llamadas a la API al iniciar un cuestionario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cuestionario con primera pregunta obtenido",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireWithFirstQuestionDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cuestionario no encontrado",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireWithFirstQuestionDto> getQuestionnaireWithFirstQuestion(
            @PathVariable Long id) throws QuestionnaireNotFoundException {
        QuestionnaireWithFirstQuestionDto questionnaire = 
            questionnaireService.getQuestionnaireWithFirstQuestion(id);
        return ResponseEntity.ok(questionnaire);
    }
    
    /**
     * Crea un nuevo cuestionario.
     * 
     * Endpoint: {@code POST /ifit/api/v1/questionnaires}
     * 
     * @param dto Datos del nuevo cuestionario
     * @return Cuestionario creado
     * @throws CoachModelTypeNotFoundException si el coach especificado no existe
     * @throws ExperienceLevelNotFoundException si el nivel especificado no existe
     * @throws QuestionNotFoundException si la primera pregunta especificada no existe
     */
    @PreAuthorize("hasRole('admin_client_role')")
    @PostMapping
    @Operation(
        summary = "Crear cuestionario",
        description = "Crea un nuevo cuestionario. Los campos coach, experienceLevel y firstQuestion son opcionales."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Cuestionario creado exitosamente",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Coach, nivel de experiencia o pregunta no encontrados",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireDto> createQuestionnaire(
            @Valid @RequestBody CreateQuestionnaireRequestDto dto) 
            throws CoachModelTypeNotFoundException, ExperienceLevelNotFoundException, QuestionNotFoundException {
        QuestionnaireDto created = questionnaireService.createQuestionnaire(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * Actualiza un cuestionario existente.
     * 
     * Endpoint: {@code PUT /ifit/api/v1/questionnaires/{id}}
     * 
     * @param id ID del cuestionario a actualizar
     * @param dto Datos a actualizar (todos los campos son opcionales)
     * @return Cuestionario actualizado
     * @throws QuestionnaireNotFoundException si no existe el cuestionario
     */
    @PreAuthorize("hasRole('admin_client_role')")
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar cuestionario",
        description = "Actualiza un cuestionario existente. Todos los campos son opcionales (actualización parcial)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cuestionario actualizado exitosamente",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cuestionario, coach, nivel o pregunta no encontrados",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireDto> updateQuestionnaire(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionnaireRequestDto dto) 
            throws QuestionnaireNotFoundException, CoachModelTypeNotFoundException, 
                   ExperienceLevelNotFoundException, QuestionNotFoundException {
        QuestionnaireDto updated = questionnaireService.updateQuestionnaire(id, dto);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * Elimina un cuestionario.
     * 
     * Endpoint: {@code DELETE /ifit/api/v1/questionnaires/{id}}
     * 
     * @param id ID del cuestionario a eliminar
     * @return Respuesta vacía con código 204
     * @throws QuestionnaireNotFoundException si no existe el cuestionario
     */
    @PreAuthorize("hasRole('admin_client_role')")
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar cuestionario",
        description = "Elimina un cuestionario del sistema. ADVERTENCIA: Esta acción no se puede deshacer."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cuestionario eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cuestionario no encontrado",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<Void> deleteQuestionnaire(@PathVariable Long id) 
            throws QuestionnaireNotFoundException {
        questionnaireService.deleteQuestionnaire(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Inicia una nueva sesión de cuestionario para el usuario autenticado.
     * 
     * Endpoint: {@code POST /ifit/api/v1/questionnaires/{questionnaireId}/start}
     * 
     * @param questionnaireId ID del cuestionario a iniciar
     * @param user Usuario autenticado (inyectado automáticamente)
     * @return Sesión iniciada con la primera pregunta
     */
    @PostMapping("/{userId}/start/{questionnaireId}")
    @Operation(
        summary = "Iniciar sesión de cuestionario",
        description = "Crea una nueva sesión de cuestionario para el usuario autenticado y devuelve la primera pregunta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Sesión de cuestionario iniciada",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cuestionario no encontrado",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireResponseDto> startQuestionnaire(
        @PathVariable Long userId,
        @PathVariable Long questionnaireId
    ) throws UserIdNotFoundException, QuestionnaireNotFoundException {
        QuestionnaireResponseDto response = questionnaireService
            .startQuestionnaire(userId, questionnaireId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Registra una respuesta a una pregunta del cuestionario.
     * 
     * Endpoint: {@code POST /ifit/api/v1/questionnaires/responses/{responseId}/answer}
     * 
     * @param responseId ID de la sesión de cuestionario
     * @param answerRequest Respuesta del usuario
     * @return Siguiente pregunta o indicación de finalización
     */
    @PostMapping("/responses/{responseId}/answer")
    @Operation(
        summary = "Responder pregunta",
        description = "Registra la respuesta del usuario a una pregunta y devuelve la siguiente pregunta según el árbol de decisión."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Respuesta registrada exitosamente",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o cuestionario ya completado",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sesión, pregunta u opción no encontrada",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireResponseDto> answerQuestion(
        @PathVariable Long responseId,
        @Valid @RequestBody AnswerRequestDto answerRequest
    ) {
        QuestionnaireResponseDto response = questionnaireService.answerQuestion(
            responseId,
            answerRequest.getQuestionId(),
            answerRequest.getSelectedOptionId(),
            answerRequest.getAdditionalText()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrocede a la pregunta anterior en una sesión de cuestionario.
     * Elimina la última respuesta registrada y devuelve esa pregunta de nuevo.
     * 
     * Endpoint: {@code POST /ifit/api/v1/questionnaires/responses/{responseId}/previous}
     * 
     * @param responseId ID de la sesión de cuestionario
     * @return La pregunta anterior con sus opciones
     */
    @PostMapping("/responses/{responseId}/previous")
    @Operation(
        summary = "Volver a la pregunta anterior",
        description = "Deshace la última respuesta registrada y devuelve esa pregunta para que el usuario pueda corregirla."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Pregunta anterior obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestionnaireResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "El cuestionario ya está completado o no hay preguntas anteriores",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sesión no encontrada",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireResponseDto> goToPreviousQuestion(
        @PathVariable Long responseId
    ) {
        QuestionnaireResponseDto response = questionnaireService.goToPreviousQuestion(responseId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene el resumen completo de una sesión de cuestionario.
     * 
     * Endpoint: {@code GET /ifit/api/v1/questionnaires/responses/{responseId}/summary}
     * 
     * @param responseId ID de la sesión
     * @return Resumen con todas las respuestas del usuario
     */
    @GetMapping("/responses/{responseId}/summary")
    @Operation(
        summary = "Obtener resumen de sesión",
        description = "Recupera el resumen completo de una sesión de cuestionario con todas las respuestas del usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resumen obtenido exitosamente",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireResponseSummaryDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sesión no encontrada",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<QuestionnaireResponseSummaryDto> getResponseSummary(
        @PathVariable Long responseId
    ) {
        QuestionnaireResponseSummaryDto summary = questionnaireService
            .getResponseSummary(responseId);
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Obtiene todas las sesiones de cuestionarios del usuario autenticado.
     * 
     * Endpoint: {@code GET /ifit/api/v1/questionnaires/responses/my-responses}
     * 
     * @param user Usuario autenticado
     * @return Lista de todas sus sesiones
     */
    @GetMapping("/responses/my-responses")
    @Operation(
        summary = "Obtener mis sesiones",
        description = "Obtiene todas las sesiones de cuestionarios del usuario autenticado (completadas y activas)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de sesiones obtenida",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<QuestionnaireResponseDto>> getMyResponses(
        @AuthenticationPrincipal AppUser user
    ) {
        return ResponseEntity.ok(questionnaireService.getUserResponses(user.getId()));
    }
    
    /**
     * Obtiene las sesiones completadas del usuario autenticado.
     * 
     * Endpoint: {@code GET /ifit/api/v1/questionnaires/responses/my-completed-responses}
     * 
     * @param user Usuario autenticado
     * @return Lista de sesiones completadas
     */
    @GetMapping("/responses/my-completed-responses")
    @Operation(
        summary = "Obtener mis sesiones completadas",
        description = "Obtiene solo las sesiones de cuestionarios completadas por el usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de sesiones completadas obtenida",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<QuestionnaireResponseDto>> getMyCompletedResponses(
        @AuthenticationPrincipal AppUser user
    ) {
        return ResponseEntity.ok(questionnaireService.getUserCompletedResponses(user.getId()));
    }
    
    /**
     * Obtiene las sesiones activas (no completadas) del usuario autenticado.
     * 
     * Endpoint: {@code GET /ifit/api/v1/questionnaires/responses/my-active-responses}
     * 
     * @param user Usuario autenticado
     * @return Lista de sesiones activas
     */
    @GetMapping("/responses/my-active-responses")
    @Operation(
        summary = "Obtener mis sesiones activas",
        description = "Obtiene las sesiones de cuestionarios que el usuario ha iniciado pero no ha completado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de sesiones activas obtenida",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = QuestionnaireResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<QuestionnaireResponseDto>> getMyActiveResponses(
        @AuthenticationPrincipal AppUser user
    ) {
        return ResponseEntity.ok(questionnaireService.getUserActiveResponses(user.getId()));
    }

    /**
     * [ADMIN] Obtiene las sesiones completadas de un usuario concreto por su ID.
     *
     * Endpoint: {@code GET /ifit/api/v1/questionnaires/responses/user/{userId}/completed}
     *
     * <p>Pensado para el panel de administración: permite a un administrador
     * consultar las sesiones de cuestionario completadas de cualquier usuario.
     * El detalle de cada sesión se obtiene con {@code /responses/{responseId}/summary}.
     *
     * @param userId ID del usuario cuyas sesiones completadas se quieren listar
     * @return Lista de sesiones completadas del usuario
     */
    @PreAuthorize("hasRole('admin_client_role')")
    @GetMapping("/responses/user/{userId}/completed")
    @Operation(
        summary = "[Admin] Sesiones completadas de un usuario",
        description = "Lista las sesiones de cuestionario completadas por el usuario indicado. Requiere rol admin."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de sesiones completadas obtenida",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = QuestionnaireResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<List<QuestionnaireResponseDto>> getCompletedResponsesByUser(
        @PathVariable Long userId
    ) {
        return ResponseEntity.ok(questionnaireService.getUserCompletedResponses(userId));
    }
}