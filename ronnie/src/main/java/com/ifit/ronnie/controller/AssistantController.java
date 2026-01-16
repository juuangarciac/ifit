package com.ifit.ronnie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.service.Eliud;
import com.ifit.ronnie.service.Kael;
import com.ifit.ronnie.service.Ronnie;
import com.ifit.ronnie.service.Serena;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para gestionar la interacción con los coaches de IA.
 * Cada coach tiene su propia especialidad y estilo de entrenamiento.
 * 
 * @author Juan García
 */
@RestController
@RequestMapping("/chat")
@Tag(
    name = "AI Coaches", 
    description = "Endpoints para interactuar con los coaches de IA de iFit. " +
                  "Cada coach está especializado en diferentes tipos de entrenamiento y ofrece " +
                  "asesoramiento personalizado basado en el historial de conversación."
)
public class AssistantController {
    
    @Autowired
    private Ronnie ronnie;

    @Autowired
    private Serena serena;

    @Autowired
    private Eliud eliud;

    @Autowired
    private Kael kael;

    @Operation(
        summary = "Chat con Ronnie",
        description = "Interactúa con Ronnie, el coach especializado en musculación y fuerza. " +
                      "Ronnie proporciona rutinas de hipertrofia, técnicas de levantamiento y " +
                      "consejos de nutrición para maximizar el desarrollo muscular."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Respuesta del coach generada exitosamente",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    value = "¡Genial! Para ganar masa muscular, te recomiendo enfocarte en ejercicios compuestos como sentadillas, press de banca y peso muerto. Entrena 4-5 días a la semana con un déficit calórico moderado..."
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    @GetMapping("/ronnie")
    public String chatWithRonnie(
            @Parameter(
                description = "ID de memoria para mantener el contexto de la conversación. " +
                              "Usa el mismo memoryId para continuar una conversación previa.",
                required = true,
                example = "12345"
            )
            @RequestParam int memoryId,
            
            @Parameter(
                description = "Mensaje del usuario para el coach",
                required = true,
                example = "¿Cómo puedo aumentar mi masa muscular?"
            )
            @RequestParam String message
    ) {
        return ronnie.chat(memoryId, message);
    }

    @Operation(
        summary = "Chat con Serena",
        description = "Interactúa con Serena, la coach especializada en yoga, flexibilidad y bienestar. " +
                      "Serena ofrece rutinas de yoga adaptadas, técnicas de respiración y " +
                      "consejos para mejorar la flexibilidad y reducir el estrés."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Respuesta del coach generada exitosamente",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    value = "Namaste. Para mejorar tu flexibilidad, te sugiero comenzar con la secuencia del saludo al sol cada mañana. Combínalo con posturas de apertura de cadera como la paloma y el guerrero..."
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    @GetMapping("/serena")
    public String chatWithSerena(
            @Parameter(
                description = "ID de memoria para mantener el contexto de la conversación",
                required = true,
                example = "12346"
            )
            @RequestParam int memoryId,
            
            @Parameter(
                description = "Mensaje del usuario para el coach",
                required = true,
                example = "Quiero mejorar mi flexibilidad, ¿qué ejercicios de yoga me recomiendas?"
            )
            @RequestParam String message
    ) {
        return serena.chat(memoryId, message);
    }

    @Operation(
        summary = "Chat con Eliud",
        description = "Interactúa con Eliud, el coach especializado en running y resistencia cardiovascular. " +
                      "Eliud proporciona planes de entrenamiento para corredores, técnicas de carrera y " +
                      "estrategias para mejorar el rendimiento en carreras de larga distancia."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Respuesta del coach generada exitosamente",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    value = "Para preparar tu primera media maratón, necesitas construir una base sólida. Comienza con 3-4 carreras por semana: una larga, una de intervalos, una de tempo y una de recuperación..."
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    @GetMapping("/eliud")
    public String chatWithEliud(
            @Parameter(
                description = "ID de memoria para mantener el contexto de la conversación",
                required = true,
                example = "12347"
            )
            @RequestParam int memoryId,
            
            @Parameter(
                description = "Mensaje del usuario para el coach",
                required = true,
                example = "Quiero correr mi primera media maratón, ¿cómo debo entrenar?"
            )
            @RequestParam String message
    ) {
        return eliud.chat(memoryId, message);
    }

    @Operation(
        summary = "Chat con Kael",
        description = "Interactúa con Kael, el coach especializado en entrenamiento funcional y HIIT. " +
                      "Kael diseña rutinas de alta intensidad, ejercicios con peso corporal y " +
                      "entrenamientos funcionales para mejorar la agilidad y quema de grasa."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Respuesta del coach generada exitosamente",
            content = @Content(
                mediaType = "text/plain",
                examples = @ExampleObject(
                    value = "¡Vamos a darle duro! Para quemar grasa con HIIT, te propongo un circuito de 20 minutos: 40 segundos de burpees, 20 de descanso, 40 segundos de mountain climbers..."
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros inválidos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    @GetMapping("/kael")
    public String chatWithKael(
            @Parameter(
                description = "ID de memoria para mantener el contexto de la conversación",
                required = true,
                example = "12348"
            )
            @RequestParam int memoryId,
            
            @Parameter(
                description = "Mensaje del usuario para el coach",
                required = true,
                example = "Necesito una rutina HIIT para quemar grasa rápidamente"
            )
            @RequestParam String message
    ) {
        return kael.chat(memoryId, message);
    }
}