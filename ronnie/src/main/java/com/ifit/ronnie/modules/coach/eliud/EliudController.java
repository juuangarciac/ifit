package com.ifit.ronnie.modules.coach.eliud;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifit.ronnie.configuration.ChatContext;
import com.ifit.ronnie.configuration.JwtUtils;
import com.ifit.ronnie.modules.coach.RoutineValidation;
import com.ifit.ronnie.modules.coach.dto.RoutineResponseDto;
import com.ifit.ronnie.modules.message.controller.dto.MessageDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/eliud")
public class EliudController {

    @Value("classpath:exercises/eliud.txt")
    private Resource exerciseCatalogResource;

    private final EliudService eliudService;
    private final EliudRoutineService eliudRoutineService;
    private final ObjectMapper objectMapper;

    public EliudController(EliudService eliudService, EliudRoutineService eliudRoutineService, ObjectMapper objectMapper) {
        this.eliudService = eliudService;
        this.eliudRoutineService = eliudRoutineService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Chat con Eliud", description = "Interactúa con Eliud, el coach especializado en running, cardio y rendimiento aeróbico. "
            + "Eliud proporciona consejos de entrenamiento, técnicas de respiración y "
            + "planes progresivos para corredores de todos los niveles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Hoy solo quiero que corras cómodo. No pienses en tiempos. Piensa en cómo se siente tu cuerpo..."))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/chat")
    public ResponseEntity<MessageDto> chatWithEliud(
            @Parameter(description = "MessageDto que contiene memoryId y message para chatear con Eliud. Usa el mismo memoryId para continuar una conversación previa.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MessageDto.class)))
            @Valid @RequestBody MessageDto messageDto,
            HttpServletRequest request) {

        try {
            ChatContext.set(JwtUtils.extractUserId(request, objectMapper), "eliud");
            String chatResponse = eliudService.chat(messageDto.memoryId(), messageDto.message());
            return ResponseEntity.ok(new MessageDto(messageDto.memoryId(), chatResponse));
        } finally {
            ChatContext.clear();
        }
    }

    @Operation(summary = "Generar rutina con Eliud", description = "Genera una rutina de entrenamiento personalizada con Eliud, "
            + "especializada en running, cardio y resistencia aeróbica. "
            + "Eliud analiza el cuestionario del usuario y genera una rutina enfocada en ejercicios "
            + "cardiovasculares y aeróbicos, adaptada al nivel y objetivos del corredor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rutina generada exitosamente", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/generate-routine")
    public ResponseEntity<RoutineResponseDto> generateRoutine(
            @Parameter(description = "MessageDto con memoryId y message con los datos del cuestionario.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MessageDto.class)))
            @Valid @RequestBody MessageDto messageDto) throws IOException {

        try {
            ChatContext.set(messageDto.userId(), "eliud");
            String catalog = exerciseCatalogResource.getContentAsString(StandardCharsets.UTF_8);
            RoutineResponseDto routineResponse = eliudRoutineService.generateRoutine(
                    messageDto.memoryId(),
                    messageDto.message(),
                    catalog);
            // Si la rutina viene degradada (mayoría de días sin ejercicios),
            // se regenera una vez y se devuelve la segunda tirada.
            if (RoutineValidation.isDegraded(routineResponse)) {
                routineResponse = eliudRoutineService.generateRoutine(
                        messageDto.memoryId(),
                        messageDto.message(),
                        catalog);
            }
            return ResponseEntity.ok(routineResponse);
        } finally {
            ChatContext.clear();
        }
    }
}
