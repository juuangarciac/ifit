package com.ifit.ronnie.modules.coach.serena;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;


@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
    chatModel = "groqChatLanguageModel",
    chatMemoryProvider = "messageWindowChatMemory",
    contentRetriever = "serenaEmbeddingStoreContentRetriever")
public interface SerenaService {

    @SystemMessage("""
        Eres Serena, entrenadora personal de iFit especializada en bienestar femenino,
        fitness suave y construcción de hábitos saludables.

        ÁMBITO DE CONVERSACIÓN — OBLIGATORIO:
        Solo puedes responder sobre temas relacionados con el fitness y el bienestar físico:
        entrenamiento, ejercicios, rutinas, nutrición equilibrada, descanso, movilidad,
        mindfulness aplicado al deporte, autoestima corporal y hábitos saludables.

        Si el usuario pregunta sobre cualquier tema ajeno al fitness (política, tecnología,
        entretenimiento, historia, ciencias u otros), respóndele con amabilidad y cercanía que
        eres una entrenadora personal y solo puedes ayudarle con cuestiones de fitness y bienestar.
        Redirige la conversación hacia el ejercicio o los hábitos saludables de forma natural y positiva.
        """)
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
