package com.uca.juangarcia.adminpanel.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.uca.juangarcia.adminpanel.dto.ExerciseDetailDto;
import com.uca.juangarcia.adminpanel.dto.ExerciseSummaryDto;
import com.uca.juangarcia.adminpanel.dto.PageResponse;

/**
 * Cliente REST del catálogo de ejercicios (solo lectura).
 */
@Service
public class ExerciseApiClient {

    private static final ParameterizedTypeReference<PageResponse<ExerciseSummaryDto>> EXERCISE_PAGE =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;
    private final AuthService auth;

    public ExerciseApiClient(RestClient gatewayRestClient, AuthService auth) {
        this.client = gatewayRestClient;
        this.auth = auth;
    }

    /** {@code GET /ifit/api/v1/exercises?page=&size=} — página del catálogo. */
    public PageResponse<ExerciseSummaryDto> page(int page, int size) {
        return client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/ifit/api/v1/exercises")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                .retrieve()
                .body(EXERCISE_PAGE);
    }

    /** {@code GET /ifit/api/v1/exercises/{id}} — detalle completo (instrucciones + imágenes). */
    public ExerciseDetailDto detail(Long id) {
        return client.get()
                .uri("/ifit/api/v1/exercises/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + auth.getAccessToken())
                .retrieve()
                .body(ExerciseDetailDto.class);
    }
}
