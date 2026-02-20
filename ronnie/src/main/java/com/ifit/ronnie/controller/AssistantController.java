package com.ifit.ronnie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.controller.dto.MessageDTO;
import com.ifit.ronnie.service.Eliud;
import com.ifit.ronnie.service.Kael;
import com.ifit.ronnie.service.Ronnie;
import com.ifit.ronnie.service.Serena;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador para gestionar la interacción con los coaches de IA.
 * Cada coach tiene su propia especialidad y estilo de entrenamiento.
 * 
 * @author Juan García
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "AI Coaches", description = "Endpoints para interactuar con los coaches de IA de iFit. " +
        "Cada coach está especializado en diferentes tipos de entrenamiento y ofrece " +
        "asesoramiento personalizado basado en el historial de conversación.")
public class AssistantController {

    @Autowired
    private Ronnie ronnie;

    @Autowired
    private Serena serena;

    @Autowired
    private Eliud eliud;

    @Autowired
    private Kael kael;

    @Operation(summary = "Chat con Ronnie", description = "Interactúa con Ronnie, el coach especializado en musculación y fuerza. "
            +
            "Ronnie proporciona rutinas de hipertrofia, técnicas de levantamiento y " +
            "consejos de nutrición para maximizar el desarrollo muscular.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "¡Genial! Para ganar masa muscular, te recomiendo enfocarte en ejercicios compuestos como sentadillas, press de banca y peso muerto. Entrena 4-5 días a la semana con un déficit calórico moderado..."))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/ronnie")
    public String chatWithRonnie(
            @Parameter(description = "MessageDTO que contiene memoryId y message para chatear con Ronnie. " +
                    "Usa el mismo memoryId para continuar una conversación previa.", required = true, content = @Content(schema = @Schema(implementation = MessageDTO.class))) @Valid @RequestBody MessageDTO messageDto) {
        System.out.println("=== RONNIE DEBUG ===");
        System.out.println("memoryId: " + messageDto.getMemoryId());
        System.out.println("message: '" + messageDto.getMessage() + "'");
        System.out.println("message length: " + messageDto.getMessage().length());
        System.out.println("===================");

        return ronnie.chat(messageDto.getMemoryId(), messageDto.getMessage());
    }

    @Operation(summary = "Chat con Serena", description = "Interactúa con Serena, la coach especializada en yoga, flexibilidad y bienestar. "
            +
            "Serena ofrece rutinas de yoga adaptadas, técnicas de respiración y " +
            "consejos para mejorar la flexibilidad y reducir el estrés.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Namaste. Para mejorar tu flexibilidad, te sugiero comenzar con la secuencia del saludo al sol cada mañana. Combínalo con posturas de apertura de cadera como la paloma y el guerrero..."))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/serena")
    public String chatWithSerena(
            @Parameter(description = "MessageDTO que contiene memoryId y message para chatear con Serena. " +
                    "Usa el mismo memoryId para continuar una conversación previa.", required = true, content = @Content(schema = @Schema(implementation = MessageDTO.class))) @Valid @RequestBody MessageDTO messageDto) {
        return serena.chat(messageDto.getMemoryId(), messageDto.getMessage());
    }

    @Operation(summary = "Chat con Eliud", description = "Interactúa con Eliud, el coach especializado en running y resistencia cardiovascular. "
            +
            "Eliud proporciona planes de entrenamiento para corredores, técnicas de carrera y " +
            "estrategias para mejorar el rendimiento en carreras de larga distancia.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Para preparar tu primera media maratón, necesitas construir una base sólida. Comienza con 3-4 carreras por semana: una larga, una de intervalos, una de tempo y una de recuperación..."))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/eliud")
    public String chatWithEliud(
            @Parameter(description = "MessageDTO que contiene memoryId y message para chatear con Eliud. " +
                    "Usa el mismo memoryId para continuar una conversación previa.", required = true, content = @Content(schema = @Schema(implementation = MessageDTO.class))) @Valid @RequestBody MessageDTO messageDto) {
        return eliud.chat(messageDto.getMemoryId(), messageDto.getMessage());
    }

    @Operation(summary = "Chat con Kael", description = "Interactúa con Kael, el coach especializado en entrenamiento funcional y HIIT. "
            +
            "Kael diseña rutinas de alta intensidad, ejercicios con peso corporal y " +
            "entrenamientos funcionales para mejorar la agilidad y quema de grasa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Respuesta del coach generada exitosamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "¡Vamos a darle duro! Para quemar grasa con HIIT, te propongo un circuito de 20 minutos: 40 segundos de burpees, 20 de descanso, 40 segundos de mountain climbers..."))),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/kael")
    public String chatWithKael(
            @Parameter(description = "MessageDTO que contiene memoryId y message para chatear con Kael. " +
                    "Usa el mismo memoryId para continuar una conversación previa.", required = true, content = @Content(schema = @Schema(implementation = MessageDTO.class))) @Valid @RequestBody MessageDTO messageDto) {
        return kael.chat(messageDto.getMemoryId(), messageDto.getMessage());
    }
}