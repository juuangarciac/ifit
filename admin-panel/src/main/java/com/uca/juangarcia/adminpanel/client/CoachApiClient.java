package com.uca.juangarcia.adminpanel.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.uca.juangarcia.adminpanel.dto.CoachModelTypeResponseDto;
import com.uca.juangarcia.adminpanel.dto.CreateCoachModelTypeRequestDto;
import com.uca.juangarcia.adminpanel.dto.UpdateCoachModelTypeRequestDto;

/**
 * Cliente REST de tipos de coach. Los writes exigen {@code admin_client_role} en el backend.
 */
@Service
public class CoachApiClient {

    private static final ParameterizedTypeReference<List<CoachModelTypeResponseDto>> COACH_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;
    private final AuthService auth;

    public CoachApiClient(RestClient gatewayRestClient, AuthService auth) {
        this.client = gatewayRestClient;
        this.auth = auth;
    }

    /** {@code GET /ifit/api/v1/coach-models/all} — todos (incluidos los deshabilitados). */
    public List<CoachModelTypeResponseDto> listAll() {
        return client.get()
                .uri("/ifit/api/v1/coach-models/all")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(COACH_LIST);
    }

    /** {@code POST /ifit/api/v1/coach-models} (admin). */
    public CoachModelTypeResponseDto create(CreateCoachModelTypeRequestDto dto) {
        return client.post()
                .uri("/ifit/api/v1/coach-models")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(CoachModelTypeResponseDto.class);
    }

    /** {@code PUT /ifit/api/v1/coach-models/{id}} (admin). */
    public CoachModelTypeResponseDto update(Long id, UpdateCoachModelTypeRequestDto dto) {
        return client.put()
                .uri("/ifit/api/v1/coach-models/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(CoachModelTypeResponseDto.class);
    }

    /** {@code DELETE /ifit/api/v1/coach-models/{id}} — deshabilita (soft) (admin). */
    public void disable(Long id) {
        client.delete()
                .uri("/ifit/api/v1/coach-models/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .toBodilessEntity();
    }

    /** {@code PATCH /ifit/api/v1/coach-models/{id}/enable} — rehabilita (admin). */
    public CoachModelTypeResponseDto enable(Long id) {
        return client.patch()
                .uri("/ifit/api/v1/coach-models/{id}/enable", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(CoachModelTypeResponseDto.class);
    }

    private String bearer() {
        return "Bearer " + auth.getAccessToken();
    }
}
