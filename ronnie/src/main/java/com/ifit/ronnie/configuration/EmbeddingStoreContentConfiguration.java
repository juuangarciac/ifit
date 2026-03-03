package com.ifit.ronnie.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;


@Configuration
public class EmbeddingStoreContentConfiguration {
    /* ChatMemoryProvider configurations */
    @Value("${chat.memory.max-messages}")
    private int maxMessages;

    @Bean("messageWindowChatMemory")
    ChatMemoryProvider dbChatMemoryStore(PersistentChatMemoryStore store){
        return memoryId -> MessageWindowChatMemory.builder()
                            .maxMessages(maxMessages)
                            .chatMemoryStore(store)
                            .id(memoryId.toString())
                            .build();
    }

    /* Personality configurations */
    @Bean("ronnieEmbeddingStoreContentRetriever")
    ContentRetriever ronnieEmbeddingStoreContentRetriever(){
        Document personality = FileSystemDocumentLoader
        .loadDocument("src/main/resources/langchain4j/assistants-personality/ronnie.txt", new TextDocumentParser());
        
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(personality, embeddingStore);
        
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    @Bean("serenaEmbeddingStoreContentRetriever")
    ContentRetriever serenaEmbeddingStoreContentRetriever(){
        Document document = FileSystemDocumentLoader
            .loadDocument("src/main/resources/langchain4j/assistants-personality/serena.txt", new TextDocumentParser());
        
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor.ingest(document, embeddingStore);
        
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    @Bean("eliudEmbeddingStoreContentRetriever")
    ContentRetriever eliudEmbeddingStoreContentRetriever(){
    Document document = FileSystemDocumentLoader
        .loadDocument("src/main/resources/langchain4j/assistants-personality/eliud.txt", new TextDocumentParser());

    InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    EmbeddingStoreIngestor.ingest(document, embeddingStore);

    return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    @Bean("kaelEmbeddingStoreContentRetriever")
    ContentRetriever kaelEmbeddingStoreContentRetriever(){
    Document document = FileSystemDocumentLoader
        .loadDocument("src/main/resources/langchain4j/assistants-personality/kael.txt", new TextDocumentParser());

    InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    EmbeddingStoreIngestor.ingest(document, embeddingStore);

    return EmbeddingStoreContentRetriever.from(embeddingStore);
    }
}
