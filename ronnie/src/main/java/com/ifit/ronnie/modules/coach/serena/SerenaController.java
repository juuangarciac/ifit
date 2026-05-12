package com.ifit.ronnie.modules.coach.serena;

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
@RequestMapping("/serena")
public class SerenaController {

    @Value("classpath:langchain4j/assistants-personality/exercises.txt")
    private Resource exerciseCatalogResource;

    private final SerenaService serenaService;
    private final SerenaRoutineService serenaRoutineService;
    private final ObjectMapper objectMapper;

    public SerenaController(SerenaService serenaService, SerenaRoutineService serenaRoutineService, ObjectMapper objectMapper) {
        this.serenaService = serenaService;
        this.serenaRoutineService = serenaRoutineService;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Chat con Serena", description = "Interactúa con Serena, la coach especializada en bienestar, tonificación y fitness accesible. "
            + "Serena ofrece rutinas adaptadas, consejos de hábitos saludables y "
            + "acompañamiento cercano para construir una vida más activa sin presión.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Hoy entrenamos juntas, ¡y sin dramas! Vamos paso a paso, lo importante es que ya estás aquí..."))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/chat")
    public ResponseEntity<MessageDto> chatWithSerena(
            @Parameter(description = "MessageDto que contiene memoryId y message para chatear con Serena. Usa el mismo memoryId para continuar una conversación previa.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MessageDto.class)))
            @Valid @RequestBody MessageDto messageDto,
            HttpServletRequest request) {

        try {
            ChatContext.set(JwtUtils.extractUserId(request, objectMapper), "serena");
            String chatResponse = serenaService.chat(messageDto.memoryId(), messageDto.message());
            return ResponseEntity.ok(new MessageDto(messageDto.memoryId(), chatResponse));
        } finally {
            ChatContext.clear();
        }
    }

    @Operation(summary = "Generar rutina con Serena", description = "Genera una rutina de entrenamiento personalizada con Serena, "
            + "especializada en bienestar, tonificación y fitness accesible. "
            + "Serena analiza el cuestionario y genera una rutina equilibrada, sin intimidar, "
            + "adaptada al nivel y objetivos de bienestar del usuario.")
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
            ChatContext.set(messageDto.userId(), "serena");
            String catalog = exerciseCatalogResource.getContentAsString(StandardCharsets.UTF_8);
            RoutineResponseDto routineResponse = serenaRoutineService.generateRoutine(
                    messageDto.memoryId(),
                    messageDto.message(),
                    catalog);
            return ResponseEntity.ok(routineResponse);
        } finally {
            ChatContext.clear();
        }
    }
}
