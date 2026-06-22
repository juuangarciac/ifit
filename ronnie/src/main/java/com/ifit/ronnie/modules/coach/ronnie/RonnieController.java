package com.ifit.ronnie.modules.coach.ronnie;

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
@RequestMapping("/ronnie")
public class RonnieController {

    @Value("classpath:exercises/ronnie.txt")
    private Resource exerciseCatalogResource;

    private final RonnieService ronnie;
    private final RonnieRoutineService ronnieRoutineService;
    private final ObjectMapper objectMapper;

    public RonnieController(RonnieService ronnie, RonnieRoutineService ronnieRoutineService, ObjectMapper objectMapper) {
        this.ronnie = ronnie;
        this.ronnieRoutineService = ronnieRoutineService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Chat con Ronnie", description = "Interactúa con Ronnie, el coach especializado en musculación e hipertrofia. "
            + "Ronnie proporciona rutinas de hipertrofia, técnicas de levantamiento y "
            + "consejos de nutrición para maximizar el desarrollo muscular.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "¡Vamos, tú puedes! Para ganar masa muscular, enfócate en los compuestos: sentadilla, press de banca y peso muerto. Paso a paso, campeón..."))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/chat")
    public ResponseEntity<MessageDto> chatWithRonnie(
            @Parameter(description = "MessageDto que contiene memoryId y message para chatear con Ronnie. Usa el mismo memoryId para continuar una conversación previa.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MessageDto.class)))
            @Valid @RequestBody MessageDto messageDto,
            HttpServletRequest request) {

        try {
            ChatContext.set(JwtUtils.extractUserId(request, objectMapper), "ronnie");
            String chatResponse = ronnie.chat(messageDto.memoryId(), messageDto.message());
            return ResponseEntity.ok(new MessageDto(messageDto.memoryId(), chatResponse));
        } finally {
            ChatContext.clear();
        }
    }

    @Operation(summary = "Generar rutina con Ronnie", description = "Genera una rutina de entrenamiento personalizada con Ronnie, "
            + "especializada en hipertrofia y fuerza muscular. "
            + "Ronnie analiza el cuestionario y genera una rutina con ejercicios compuestos "
            + "y de aislamiento, distribuida por grupos musculares.")
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
            ChatContext.set(messageDto.userId(), "ronnie");
            String catalog = exerciseCatalogResource.getContentAsString(StandardCharsets.UTF_8);
            RoutineResponseDto routineResponse = ronnieRoutineService.generateRoutine(
                    messageDto.memoryId(),
                    messageDto.message(),
                    catalog);
            // Si la rutina viene degradada (mayoría de días sin ejercicios),
            // se regenera una vez y se devuelve la segunda tirada.
            if (RoutineValidation.isDegraded(routineResponse)) {
                routineResponse = ronnieRoutineService.generateRoutine(
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
