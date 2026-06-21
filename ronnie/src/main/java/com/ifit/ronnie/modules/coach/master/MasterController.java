package com.ifit.ronnie.modules.coach.master;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.configuration.ChatContext;
import com.ifit.ronnie.modules.coach.dto.RoutineResponseDto;
import com.ifit.ronnie.modules.message.controller.dto.MessageDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/master")
public class MasterController {

        @Value("classpath:exercises.txt")
        private Resource masterCatalogResource;
        
        private final Master master;

        public MasterController(Master master) {
            this.master = master;
        }

        @Operation(summary = "Generar rutina con Ronnie", description = "Genera una rutina de entrenamiento personalizada con Ronnie. "
                        + "Ronnie analiza el cuestionario del usuario y genera una rutina estructurada " 
                        + "basada en ejercicios de su base de datos, adaptada al nivel, objetivos y disponibilidad.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Rutina generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"message\": \"¡Rutina personalizada generada!\", \"routine\": {...}}"))),
                @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PostMapping("/generate-routine")
        public ResponseEntity<RoutineResponseDto> generateRoutine(
                @Parameter(description = "MessageDto que contiene memoryId y message con los datos"
                                 + "del cuestionario para generar la rutina. Usa el mismo memoryId para mantener el contexto de la conversación."
                        , required = true, 
                        content = @Content(schema = @Schema(implementation = MessageDto.class))) 
        @Valid @RequestBody MessageDto messageDto)
        throws IOException {

                try {
                        ChatContext.set(messageDto.userId(), "master");
                        String catalog = masterCatalogResource.getContentAsString(StandardCharsets.UTF_8);

                        RoutineResponseDto routineResponse = master.generateRoutine(
                                messageDto.memoryId(),
                                messageDto.message(),
                                catalog);

                        return ResponseEntity.ok(routineResponse);
                } finally {
                        ChatContext.clear();
                }
        }
}
