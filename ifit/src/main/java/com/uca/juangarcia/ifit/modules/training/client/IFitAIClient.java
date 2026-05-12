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

import com.uca.juangarcia.ifit.modules.training.client.dto.IFitAIMaxMemoryIdResponseDto;
import com.uca.juangarcia.ifit.modules.training.client.dto.IFitAIRoutineResponseDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RonnieMessageDto;
import com.uca.juangarcia.ifit.modules.training.controller.dto.RoutineResponseDto;
import com.uca.juangarcia.ifit.modules.training.model.CoachType;


/**
 * Cliente HTTP para comunicarse con el microservicio Ronnie.
 * 
 * Maneja las llamadas al servicio de IA para generar rutinas personalizadas.
 */
@Component
public class IFitAIClient {

    private static final Logger logger = LoggerFactory.getLogger(IFitAIClient.class);

    private final RestTemplate restTemplate;
    private final String ronnieBaseUrl;

    public IFitAIClient(
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
    public RoutineResponseDto generateRoutine(int memoryId, String prompt, String keycloakUserId, CoachType coachType) {
        String url = ronnieBaseUrl + coachType.getEndpointPath();

        logger.debug("Calling Ronnie service at: {}", url);
        logger.debug("MemoryId: {}, Coach: {}, Prompt length: {} characters", memoryId, coachType, prompt.length());

        try {
            // Preparar request body
            RonnieMessageDto messageDto = new RonnieMessageDto(memoryId, prompt, keycloakUserId);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RonnieMessageDto> requestEntity = new HttpEntity<>(messageDto, headers);

            // Realizar llamada HTTP
            ResponseEntity<IFitAIRoutineResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    IFitAIRoutineResponseDto.class);

             IFitAIRoutineResponseDto responseBody = response.getBody();

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

    /**
     * Obtiene el ID máximo de memoria desde el servicio de Ronnie para mantener el contexto de conversación.
     * @return DTO con el ID máximo de memoria
     */
    public IFitAIMaxMemoryIdResponseDto getMaxMemoryId() {
        String url = ronnieBaseUrl + "/messages/max-memory-id";

        logger.debug("Calling Ronnie service for max memory ID at: {}", url);

        try {
            ResponseEntity<IFitAIMaxMemoryIdResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    IFitAIMaxMemoryIdResponseDto.class);

            IFitAIMaxMemoryIdResponseDto responseBody = response.getBody();

            logger.info("Max memory ID retrieved successfully: {}", responseBody != null ? responseBody.toString() : "null");

            return responseBody;

        } catch (HttpClientErrorException e) {
            logger.error("Client error calling Ronnie service for max memory ID: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error calling Ronnie service for max memory ID: " + e.getMessage(), e);

        } catch (HttpServerErrorException e) {
            logger.error("Server error in Ronnie service for max memory ID: {} - {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Ronnie service error for max memory ID: " + e.getMessage(), e);

        } catch (Exception e) {
            logger.error("Unexpected error calling Ronnie service for max memory ID", e);
            throw new RuntimeException("Failed to retrieve max memory ID: " + e.getMessage(), e);
        }
    }
}