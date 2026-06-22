package com.ifit.ronnie.modules.coach.kael;

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
@RequestMapping("/kael")
public class KaelController {

    @Value("classpath:exercises/kael.txt")
    private Resource exerciseCatalogResource;

    private final KaelService kaelService;
    private final KaelRoutineService kaelRoutineService;
    private final ObjectMapper objectMapper;

    public KaelController(KaelService kaelService, KaelRoutineService kaelRoutineService, ObjectMapper objectMapper) {
        this.kaelService = kaelService;
        this.kaelRoutineService = kaelRoutineService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Chat con Kael", description = "Interactúa con Kael, el coach especializado en calistenia, street workout y fuerza funcional. "
            + "Kael diseña rutinas con el peso corporal, circuitos HIIT y progresiones técnicas "
            + "para entrenar en cualquier lugar sin depender de máquinas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "No necesitas un gimnasio para ponerte fuerte, solo disciplina y constancia..."))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/chat")
    public ResponseEntity<MessageDto> chatWithKael(
            @Parameter(description = "MessageDto que contiene memoryId y message para chatear con Kael. Usa el mismo memoryId para continuar una conversación previa.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MessageDto.class)))
            @Valid @RequestBody MessageDto messageDto,
            HttpServletRequest request) {

        try {
            ChatContext.set(JwtUtils.extractUserId(request, objectMapper), "kael");
            String chatResponse = kaelService.chat(messageDto.memoryId(), messageDto.message());
            return ResponseEntity.ok(new MessageDto(messageDto.memoryId(), chatResponse));
        } finally {
            ChatContext.clear();
        }
    }

    @Operation(summary = "Generar rutina con Kael", description = "Genera una rutina de entrenamiento personalizada con Kael, "
            + "especializada en calistenia, street workout y fuerza funcional con el peso corporal. "
            + "Kael analiza el cuestionario y genera una rutina con ejercicios sin maquinaria, "
            + "adaptada al nivel y entorno del usuario.")
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
            ChatContext.set(messageDto.userId(), "kael");
            String catalog = exerciseCatalogResource.getContentAsString(StandardCharsets.UTF_8);
            RoutineResponseDto routineResponse = kaelRoutineService.generateRoutine(
                    messageDto.memoryId(),
                    messageDto.message(),
                    catalog);
            // Si la rutina viene degradada (mayoría de días sin ejercicios),
            // se regenera una vez y se devuelve la segunda tirada.
            if (RoutineValidation.isDegraded(routineResponse)) {
                routineResponse = kaelRoutineService.generateRoutine(
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
