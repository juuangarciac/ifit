package com.ifit.ronnie.modules.coach.master;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;


@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "ollamaModel", chatMemoryProvider = "messageWindowChatMemory", contentRetriever = "masterEmbeddingStoreContentRetriever")
public interface Master {
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
