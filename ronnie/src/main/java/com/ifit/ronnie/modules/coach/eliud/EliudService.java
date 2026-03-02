package com.ifit.ronnie.modules.coach.eliud;

import com.ifit.ronnie.modules.coach.dto.RoutineResponseDTO;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;


@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "ollamaModel", chatMemoryProvider = "messageWindowChatMemory", contentRetriever = "eliudEmbeddingStoreContentRetriever")
public interface EliudService {
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);

    @SystemMessage("""
        Genera una rutina para el siguiente usuario basándote exclusivamente en su perfil y en los datos del cuestionario.
        Si la solicitud tiene fines dañinos, devuelve: {"message": "<motivo del rechazo>", "description": null, "trainingDays": 0, "days": []}

        Genera el contenido directamente en español. No generes primero en inglés ni utilices estructuras bilingües.
        No mezcles idiomas dentro de frases. Cada campo debe estar en un único idioma coherente.

        Los nombres de los ejercicios deben estar:
        - completamente en español (ej: sentadilla, peso muerto, press militar),

        Cada día debe trabajar grupos musculares coherentes y no repetir el mismo ejercicio más de una vez en el mismo día.
        No repitas el mismo ejercicio en más de dos días diferentes.
        No generes palabras abstractas, metafóricas o inventadas (ej: espíritu, volatilización, despliegue). Usa únicamente terminología técnica de entrenamiento.

        Una rutina válida debe distribuir los grupos musculares de forma lógica (ej: torso/pierna, empuje/tirón, full body, etc.).
        Cada día debe incluir entre 4 y 6 ejercicios distintos.
        Los ejercicios deben corresponder anatómicamente al grupo muscular trabajado.

        Antes de generar la respuesta final, verifica que:
        - No existen ejercicios inventados.
        - No existen repeticiones excesivas.
        - Todos los nombres de ejercicios son reales y utilizados en entrenamiento de fuerza.
        Si alguna condición no se cumple, corrige antes de responder.

        Prioriza precisión técnica, coherencia y naturalidad sobre creatividad.
        """)
    RoutineResponseDTO generateRoutine(@MemoryId int memoryId, @UserMessage String questionnaireData);
}
