package com.ifit.ronnie.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

@Configuration
public class GroqModelConfiguration {
        // Cambia las @Value
    @Value("${groq.base-url}")
    private String groqBaseUrl;

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.model-name}")
    private String modelName;

    @Value("${groq.timeout:120}")
    private int timeout;

    // Cambia el bean
    @Bean("groqChatLanguageModel")
    ChatLanguageModel groqChatLanguageModel(){
        return OpenAiChatModel.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(timeout))
                .temperature(0.3)
                .build();
    }

    @Bean("groqJsonChatLanguageModel")
    ChatLanguageModel groqJsonChatLanguageModel(){
        return OpenAiChatModel.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(timeout))
                .responseFormat("json_object")
                .temperature(0.3)
                .build();
    }
}
