package com.ifit.ronnie.modules.coach.serena;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.modules.coach.dto.RoutineResponseDTO;
import com.ifit.ronnie.modules.message.controller.dto.MessageDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/serena")
public class SerenaController {
    @Autowired
        private SerenaService serenaService;

        @Operation(summary = "Chat con Serena", description = "Interactúa con Serena, la coach especializada en yoga, flexibilidad y bienestar. "
                        +
                        "Serena ofrece rutinas de yoga adaptadas, técnicas de respiración y " +
                        "consejos para mejorar la flexibilidad y reducir el estrés.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "¡Genial! Para ganar masa muscular, te recomiendo enfocarte en ejercicios compuestos como sentadillas, press de banca y peso muerto. Entrena 4-5 días a la semana con un déficit calórico moderado..."))),
                        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PostMapping("/chat")
        public String chatWithSerena(
                        @Parameter(description = "MessageDTO que contiene memoryId y message para chatear con Serena. Usa el mismo memoryId para continuar una conversación previa.", required = true, content = @Content(schema = @Schema(implementation = MessageDTO.class))) @Valid @RequestBody MessageDTO messageDto) {

                return serenaService.chat(messageDto.getMemoryId(), messageDto.getMessage());
        }

        @Operation(summary = "Generar rutina con Serena", description = "Genera una rutina de entrenamiento personalizada con Serena. "
                        +
                        "Serena analiza el cuestionario del usuario y genera una rutina estructurada " +
                        "basada en ejercicios de su base de datos, adaptada al nivel, objetivos y disponibilidad.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rutina generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"¡Rutina personalizada generada!\", \"routine\": {...}}"))),
                        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PostMapping("/generate-routine")
        public ResponseEntity<RoutineResponseDTO> generateRoutine(
                        @Parameter(description = "MessageDTO que contiene memoryId y message con los datos del cuestionario para generar la rutina. Usa el mismo memoryId para mantener el contexto de la conversación.", required = true, content = @Content(schema = @Schema(implementation = MessageDTO.class))) @Valid @RequestBody MessageDTO messageDto) {

                RoutineResponseDTO routineResponse = serenaService.generateRoutine(messageDto.getMemoryId(),
                                messageDto.getMessage());
                return ResponseEntity.ok(routineResponse);
        }
}
