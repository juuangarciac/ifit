package com.ifit.ronnie.modules.coach.eliud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.modules.message.controller.dto.MessageDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/eliud")
public class EliudController {
     @Autowired
        private EliudService eliudService;

        @Operation(summary = "Chat con Eliud", description = "Interactúa con Eliud, el coach especializado en musculación y fuerza. "
                        +
                        "Eliud proporciona rutinas de hipertrofia, técnicas de levantamiento y " +
                        "consejos de nutrición para maximizar el desarrollo muscular.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "¡Genial! Para ganar masa muscular, te recomiendo enfocarte en ejercicios compuestos como sentadillas, press de banca y peso muerto. Entrena 4-5 días a la semana con un déficit calórico moderado..."))),
                        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
                        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
        })
        @PostMapping("/chat")
        public String chatWithEliud(
                        @Parameter(description = "MessageDTO que contiene memoryId y message para chatear con Eliud. Usa el mismo memoryId para continuar una conversación previa.", 
                        required = true, 
                        content = @Content(schema = @Schema(implementation = MessageDTO.class))) 
                        @Valid 
                        @RequestBody MessageDTO messageDto) {

                return eliudService.chat(messageDto.getMemoryId(), messageDto.getMessage());
        }
}
