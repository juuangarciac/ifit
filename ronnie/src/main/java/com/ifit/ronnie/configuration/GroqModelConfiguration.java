package com.ifit.ronnie.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Modelos servidos por Groq (API compatible con OpenAI).
 *
 * <p>Tres beans, separados por tarea:
 * <ul>
 *   <li>{@code groqLlama70b}      — llama-3.3-70b, texto plano. CHAT conversacional.</li>
 *   <li>{@code groqLlama70bJson}  — llama-3.3-70b, respuesta JSON. Disponible para
 *       tareas estructuradas ligeras.</li>
 *   <li>{@code groqGptOssJson}    — gpt-oss-120b (OpenAI), respuesta JSON. GENERACIÓN
 *       DE RUTINAS (la tarea más exigente).</li>
 * </ul>
 */
@Configuration
public class GroqModelConfiguration {

    @Value("${groq.base-url}")
    private String groqBaseUrl;

    @Value("${groq.api-key}")
    private String groqApiKey;

    /** Modelo llama (chat y json ligero). */
    @Value("${groq.model-name}")
    private String llamaModelName;

    /** Modelo para generación de rutinas (gpt-oss). Fallback a groq.model-name. */
    @Value("${groq.routine-model-name:${groq.model-name}}")
    private String routineModelName;

    @Value("${groq.timeout:120}")
    private int timeout;

    /** Groq · llama-3.3-70b · texto plano. Para el CHAT conversacional de los coaches. */
    @Bean("groqLlama70b")
    ChatLanguageModel groqLlama70b() {
        return OpenAiChatModel.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .modelName(llamaModelName)
                .timeout(Duration.ofSeconds(timeout))
                .temperature(0.5)
                .build();
    }

    /** Groq · llama-3.3-70b · respuesta JSON. Disponible para tareas estructuradas ligeras. */
    @Bean("groqLlama70bJson")
    ChatLanguageModel groqLlama70bJson() {
        return OpenAiChatModel.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .modelName(llamaModelName)
                .timeout(Duration.ofSeconds(timeout))
                .responseFormat("json_object")
                .temperature(0.3)
                .build();
    }

    /** Groq · gpt-oss-120b (OpenAI) · respuesta JSON. Para la GENERACIÓN DE RUTINAS. */
    @Bean("groqGptOssJson")
    ChatLanguageModel groqGptOssJson() {
        return OpenAiChatModel.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .modelName(routineModelName)
                .timeout(Duration.ofSeconds(timeout))
                .responseFormat("json_object")
                .temperature(0.3)
                .build();
    }
}
