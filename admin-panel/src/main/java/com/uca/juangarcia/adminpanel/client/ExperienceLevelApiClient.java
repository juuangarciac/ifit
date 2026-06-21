package com.uca.juangarcia.adminpanel.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.uca.juangarcia.adminpanel.dto.CreateExperienceLevelDto;
import com.uca.juangarcia.adminpanel.dto.ExperienceLevelDto;
import com.uca.juangarcia.adminpanel.dto.UpdateExperienceLevelDto;

/**
 * Cliente REST de niveles de experiencia. Los writes exigen {@code admin_client_role}.
 */
@Service
public class ExperienceLevelApiClient {

    private static final ParameterizedTypeReference<List<ExperienceLevelDto>> LEVEL_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;
    private final AuthService auth;

    public ExperienceLevelApiClient(RestClient gatewayRestClient, AuthService auth) {
        this.client = gatewayRestClient;
        this.auth = auth;
    }

    /** {@code GET /ifit/api/v1/experience-levels}. */
    public List<ExperienceLevelDto> listAll() {
        return client.get()
                .uri("/ifit/api/v1/experience-levels")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(LEVEL_LIST);
    }

    /** {@code POST /ifit/api/v1/experience-levels} (admin). */
    public ExperienceLevelDto create(CreateExperienceLevelDto dto) {
        return client.post()
                .uri("/ifit/api/v1/experience-levels")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(ExperienceLevelDto.class);
    }

    /** {@code PATCH /ifit/api/v1/experience-levels/{id}} — solo descripción (admin). */
    public ExperienceLevelDto update(Long id, UpdateExperienceLevelDto dto) {
        return client.patch()
                .uri("/ifit/api/v1/experience-levels/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(ExperienceLevelDto.class);
    }

    /** {@code DELETE /ifit/api/v1/experience-levels/{id}} (admin). */
    public void delete(Long id) {
        client.delete()
                .uri("/ifit/api/v1/experience-levels/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .toBodilessEntity();
    }

    private String bearer() {
        return "Bearer " + auth.getAccessToken();
    }
}
