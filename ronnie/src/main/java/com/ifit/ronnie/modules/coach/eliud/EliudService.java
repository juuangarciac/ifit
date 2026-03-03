package com.ifit.ronnie.modules.coach.eliud;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;


@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "groqChatLanguageModel", chatMemoryProvider = "messageWindowChatMemory", contentRetriever = "eliudEmbeddingStoreContentRetriever")
public interface EliudService {
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
