package com.ifit.ronnie.modules.coach.ronnie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.modules.coach.dto.RoutineResponseDTO;
import com.ifit.ronnie.modules.message.controller.dto.MessageDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

/**
 * Controlador para gestionar las interacciones con el coach Ronnie
 */
@RestController
@RequestMapping("/ronnie")
public class RonnieController {

        @Autowired
        private RonnieService ronnie;

        @Operation(summary = "Chat con Ronnie", description = "Interactúa con Ronnie, el coach especializado en musculación y fuerza. "
                        +
                        "Ronnie proporciona rutinas de hipertrofia, técnicas de levantamiento y " +
                        "consejos de nutrición para maximizar el desarrollo muscular.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "¡Genial! Para ganar masa muscular, te recomiendo enfocarte en ejercicios compuestos como sentadillas, press de banca y peso muerto. Entrena 4-5 días a la semana con un déficit calórico moderado..."))),
                        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PostMapping("/chat")
        public String chatWithRonnie(
                        @Parameter(description = "MessageDTO que contiene memoryId y message para chatear con Ronnie. Usa el mismo memoryId para continuar una conversación previa.", required = true, content = @Content(schema = @Schema(implementation = MessageDTO.class))) @Valid @RequestBody MessageDTO messageDto) {

                return ronnie.chat(messageDto.getMemoryId(), messageDto.getMessage());
        }

        @Operation(summary = "Generar rutina con Ronnie", description = "Genera una rutina de entrenamiento personalizada con Ronnie. "
                        +
                        "Ronnie analiza el cuestionario del usuario y genera una rutina estructurada " +
                        "basada en ejercicios de su base de datos, adaptada al nivel, objetivos y disponibilidad.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Rutina generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"¡Rutina personalizada generada!\", \"routine\": {...}}"))),
                        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PostMapping("/generate-routine")
        public ResponseEntity<RoutineResponseDTO> generateRoutine(
                        @Parameter(description = "MessageDTO que contiene memoryId y message con los datos del cuestionario para generar la rutina. Usa el mismo memoryId para mantener el contexto de la conversación.", required = true, content = @Content(schema = @Schema(implementation = MessageDTO.class))) @Valid @RequestBody MessageDTO messageDto) {

                RoutineResponseDTO routineResponse = ronnie.generateRoutine(messageDto.getMemoryId(),
                                messageDto.getMessage());
                return ResponseEntity.ok(routineResponse);
        }
}