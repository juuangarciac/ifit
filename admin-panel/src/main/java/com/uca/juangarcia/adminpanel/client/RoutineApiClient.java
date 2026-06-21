package com.uca.juangarcia.adminpanel.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.uca.juangarcia.adminpanel.dto.RoutineResponseDto;

/**
 * Cliente REST de rutinas (a través del Gateway).
 */
@Service
public class RoutineApiClient {

    private static final ParameterizedTypeReference<List<RoutineResponseDto>> ROUTINE_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;
    private final AuthService auth;

    public RoutineApiClient(RestClient gatewayRestClient, AuthService auth) {
        this.client = gatewayRestClient;
        this.auth = auth;
    }

    /** {@code GET /ifit/api/v1/routines/user/{userId}} — rutinas de un cliente. */
    public List<RoutineResponseDto> byUser(Long userId) {
        return client.get()
                .uri("/ifit/api/v1/routines/user/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(ROUTINE_LIST);
    }

    /** {@code PATCH /ifit/api/v1/routines/{id}/toggle-active?isActive=...} — activa/desactiva. */
    public RoutineResponseDto toggleActive(Long id, boolean active) {
        return client.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/ifit/api/v1/routines/{id}/toggle-active")
                        .queryParam("isActive", active)
                        .build(id))
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(RoutineResponseDto.class);
    }

    /** {@code DELETE /ifit/api/v1/routines/{id}} — borrado físico de la rutina. */
    public void delete(Long id) {
        client.delete()
                .uri("/ifit/api/v1/routines/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .toBodilessEntity();
    }

    private String bearer() {
        return "Bearer " + auth.getAccessToken();
    }
}
