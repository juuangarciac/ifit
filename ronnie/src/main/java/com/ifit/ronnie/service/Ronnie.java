package com.ifit.ronnie.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;


@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "ollamaModel", chatMemoryProvider = "messageWindowChatMemory", contentRetriever = "ronnieEmbeddingStoreContentRetriever")
public interface Ronnie {
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);
}
