package com.ifit.ronnie.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.ollama.OllamaChatModel;

@Configuration
public class OllamaModelConfiguration {

    /* Ollama Model configurations */
    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model-name}")
    private String modelName;

    @Value("${ollama.timeout:120}")
    private int timeout;

    @Bean("ollamaModel")
    ChatLanguageModel ollamaChatLanguageModel(){
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(timeout))
                .responseFormat(ResponseFormat.JSON)
                .temperature(0.3)
                .build();
    }
}
