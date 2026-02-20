package com.uca.juangarcia.ifit.modules.training.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.uca.juangarcia.ifit.modules.training.client.dto.RonnieRoutineResponseDTO;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RonnieMessageDTO;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineResponseDto;

/**
 * Cliente HTTP para comunicarse con el microservicio Ronnie.
 * 
 * Maneja las llamadas al servicio de IA para generar rutinas personalizadas.
 */
@Component
public class RonnieClient {

    private static final Logger logger = LoggerFactory.getLogger(RonnieClient.class);

    private final RestTemplate restTemplate;
    private final String ronnieBaseUrl;

    public RonnieClient(
            RestTemplate restTemplate,
            @Value("${ronnie.service.url:http://localhost:8082}") String ronnieBaseUrl) {
        this.restTemplate = restTemplate;
        this.ronnieBaseUrl = ronnieBaseUrl;
    }

    /**
     * Genera una rutina personalizada llamando al servicio de Ronnie.
     * 
     * @param memoryId ID de memoria para mantener contexto de conversación
     * @param prompt   Prompt construido con la información del cuestionario
     * @return JSON string con la rutina generada
     * @throws RuntimeException si hay error en la comunicación
     */
    public RoutineResponseDto generateRoutine(int memoryId, String prompt) {
        String url = ronnieBaseUrl + "/ronnie/generate-routine";

        logger.debug("Calling Ronnie service at: {}", url);
        logger.debug("MemoryId: {}, Prompt length: {} characters", memoryId, prompt.length());

        try {
            // Preparar request body
            RonnieMessageDTO messageDto = new RonnieMessageDTO(memoryId, prompt);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RonnieMessageDTO> requestEntity = new HttpEntity<>(messageDto, headers);

            // Realizar llamada HTTP
            ResponseEntity<RonnieRoutineResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    RonnieRoutineResponseDTO.class);

            RonnieRoutineResponseDTO responseBody = response.getBody();

            logger.info("Routine generated successfully. Response length: {} characters",
                    responseBody != null ? responseBody.toString().length() : 0);

            return responseBody.toRoutineResponseDto();

        } catch (HttpClientErrorException e) {
            logger.error("Client error calling Ronnie service: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error calling Ronnie service: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            logger.error("Server error in Ronnie service: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Ronnie service error: " + e.getMessage(), e);

        } catch (Exception e) {
            logger.error("Unexpected error calling Ronnie service", e);
            throw new RuntimeException("Failed to generate routine: " + e.getMessage(), e);
        }
    }
}