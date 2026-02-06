package com.ifit.ronnie.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "ollamaModel", chatMemoryProvider = "messageWindowChatMemory", contentRetriever = "ronnieEmbeddingStoreContentRetriever")
public interface Ronnie {
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);

    @SystemMessage("""
            DEBES responder ÚNICAMENTE con un objeto JSON válido con la siguiente estructura:
            {
                "message": "Mensaje motivacional personalizado para el usuario",
                "routine": {
                    "userId": "ID del usuario",
                    "description": "Descripción general de la rutina",
                    "trainingDays": número_de_días,
                    "days": [
                        {
                            "dayNumber": 1,
                            "dayName": "Día 1 - Pecho y Tríceps",
                            "exercises": [
                                {
                                    "exerciseId": "id_del_ejercicio",
                                    "exerciseName": "nombre_del_ejercicio",
                                    "sets": 4,
                                    "reps": "10-12",
                                    "restSeconds": 90,
                                    "notes": "notas específicas"
                                }
                            ]
                        }
                    ]
                }
            }
            Crea la rutina apropiada, en base a la informacion proporcionada por el usuario.
            """)
    String generateRoutine(@MemoryId int memoryId, @UserMessage String questionnaireData);
}
