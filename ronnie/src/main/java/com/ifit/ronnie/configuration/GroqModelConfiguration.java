package com.ifit.ronnie.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * Configuración de los modelos LLM del microservicio Ronnie.
 *
 * <p>Todos los modelos se sirven a través de <b>Groq</b>, cuya API es compatible
 * con la de OpenAI; por eso se usa {@link OpenAiChatModel} apuntando a la
 * {@code base-url} de Groq. El sistema usa <b>EXACTAMENTE DOS modelos</b>, uno
 * por tarea:
 *
 * <ul>
 *   <li><b>{@code groqLlama70b}</b> → modelo {@code llama-3.3-70b-versatile}.
 *       CHAT conversacional de los coaches. Texto plano, temperatura media.</li>
 *   <li><b>{@code groqGptOssJson}</b> → modelo {@code openai/gpt-oss-120b}.
 *       GENERACIÓN DE RUTINAS. JSON estricto y temperatura baja para máxima
 *       fidelidad al catálogo de ejercicios y al formato pedido.</li>
 * </ul>
 *
 * <p>Cada bean se referencia por su nombre desde los {@code @AiService}
 * (atributo {@code chatModel}). Los nombres reales de los modelos se definen en
 * {@code application.properties}: {@code groq.model-name} (chat) y
 * {@code groq.routine-model-name} (rutinas).
 */
@Configuration
public class GroqModelConfiguration {

    /** URL base de la API de Groq (compatible OpenAI). Ej.: https://api.groq.com/openai/v1 */
    @Value("${groq.base-url}")
    private String groqBaseUrl;

    /** Clave de API de Groq. Se inyecta desde la variable de entorno GROQ_API_KEY. */
    @Value("${groq.api-key}")
    private String groqApiKey;

    /** Modelo de CHAT → {@code llama-3.3-70b-versatile}. */
    @Value("${groq.model-name}")
    private String chatModelName;

    /**
     * Modelo de GENERACIÓN DE RUTINAS → {@code openai/gpt-oss-120b}.
     * Si no se define, cae por defecto al modelo de chat ({@code groq.model-name}).
     */
    @Value("${groq.routine-model-name:${groq.model-name}}")
    private String routineModelName;

    /** Timeout de las llamadas al LLM, en segundos. */
    @Value("${groq.timeout:120}")
    private int timeout;

    /**
     * MODELO DE CHAT — conversación con los coaches.
     *
     * <ul>
     *   <li><b>Modelo:</b> {@code llama-3.3-70b-versatile} (Groq).</li>
     *   <li><b>Formato:</b> texto plano (sin JSON); la respuesta es un mensaje de chat.</li>
     *   <li><b>Temperatura 0.5:</b> respuestas naturales y con variedad, sin
     *       desbordarse en creatividad.</li>
     * </ul>
     *
     * <p><b>Usado por:</b> RonnieService, EliudService, SerenaService, KaelService.
     */
    @Bean("groqLlama70b")
    ChatLanguageModel groqLlama70b() {
        return OpenAiChatModel.builder()
                .baseUrl(groqBaseUrl)
                .apiKey(groqApiKey)
                .modelName(chatModelName)
                .timeout(Duration.ofSeconds(timeout))
                .temperature(0.5)
                .build();
    }

    /**
     * MODELO DE GENERACIÓN DE RUTINAS — la tarea más exigente.
     *
     * <ul>
     *   <li><b>Modelo:</b> {@code openai/gpt-oss-120b} (Groq). Razonador grande,
     *       mejor siguiendo la lista cerrada de ejercicios del catálogo.</li>
     *   <li><b>Formato:</b> {@code json_object} — fuerza una salida JSON estricta
     *       que mapea directamente a {@code RoutineResponseDto}.</li>
     *   <li><b>Temperatura 0.3:</b> baja, para maximizar la fidelidad al catálogo
     *       y al esquema JSON, minimizando alucinaciones de nombres de ejercicios.</li>
     * </ul>
     *
     * <p><b>Usado por:</b> Master, RonnieRoutineService, EliudRoutineService,
     * SerenaRoutineService, KaelRoutineService.
     */
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
