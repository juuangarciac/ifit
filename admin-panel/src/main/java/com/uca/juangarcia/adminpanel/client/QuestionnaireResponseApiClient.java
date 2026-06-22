package com.uca.juangarcia.adminpanel.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.uca.juangarcia.adminpanel.dto.QuestionnaireResponseDto;
import com.uca.juangarcia.adminpanel.dto.QuestionnaireResponseSummaryDto;

/**
 * Cliente REST para las sesiones (respuestas) de cuestionarios.
 * El listado por usuario exige {@code admin_client_role} en el backend.
 */
@Service
public class QuestionnaireResponseApiClient {

    private static final ParameterizedTypeReference<List<QuestionnaireResponseDto>> RESPONSE_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;
    private final AuthService auth;

    public QuestionnaireResponseApiClient(RestClient gatewayRestClient, AuthService auth) {
        this.client = gatewayRestClient;
        this.auth = auth;
    }

    /**
     * {@code GET /ifit/api/v1/questionnaires/responses/user/{userId}/completed}
     * — sesiones completadas de un usuario (admin).
     */
    public List<QuestionnaireResponseDto> listCompletedByUser(Long userId) {
        return client.get()
                .uri("/ifit/api/v1/questionnaires/responses/user/{userId}/completed", userId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(RESPONSE_LIST);
    }

    /**
     * {@code GET /ifit/api/v1/questionnaires/responses/{responseId}/summary}
     * — detalle completo de una sesión (cuestionario, fechas y respuestas).
     */
    public QuestionnaireResponseSummaryDto getSummary(Long responseId) {
        return client.get()
                .uri("/ifit/api/v1/questionnaires/responses/{responseId}/summary", responseId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(QuestionnaireResponseSummaryDto.class);
    }

    private String bearer() {
        return "Bearer " + auth.getAccessToken();
    }
}
