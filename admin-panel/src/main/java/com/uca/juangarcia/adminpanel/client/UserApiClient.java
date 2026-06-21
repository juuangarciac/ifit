package com.uca.juangarcia.adminpanel.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.uca.juangarcia.adminpanel.dto.AppUserResponseDto;
import com.uca.juangarcia.adminpanel.dto.RegisterRequestDto;
import com.uca.juangarcia.adminpanel.dto.UpdateAppUserRequestDto;

/**
 * Cliente REST de usuarios (a través del Gateway). Todas las llamadas envían
 * {@code Authorization: Bearer <accessToken>}; el backend exige {@code admin_client_role}.
 */
@Service
public class UserApiClient {

    private static final ParameterizedTypeReference<List<AppUserResponseDto>> USER_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient client;
    private final AuthService auth;

    public UserApiClient(RestClient gatewayRestClient, AuthService auth) {
        this.client = gatewayRestClient;
        this.auth = auth;
    }

    /** {@code GET /ifit/api/v1/users} — lista completa de clientes. */
    public List<AppUserResponseDto> listAll() {
        return client.get()
                .uri("/ifit/api/v1/users")
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .body(USER_LIST);
    }

    /**
     * {@code POST /ifit/api/v1/auth/register} — da de alta un nuevo cliente.
     *
     * <p>Usa el endpoint de registro (no {@code POST /users}) porque es el único que crea la
     * identidad de forma coherente en Keycloak + BD de forma transaccional y envía el email de
     * verificación. Es una ruta <strong>pública</strong> ({@code IFIT-PUBLIC}), así que no requiere
     * {@code Authorization}. El nuevo usuario queda <em>sin verificar</em> hasta que confirme su email.
     *
     * @param dto datos del nuevo cliente (nombre, apellido, email, contraseña)
     */
    public void create(RegisterRequestDto dto) {
        client.post()
                .uri("/ifit/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    /** {@code PUT /ifit/api/v1/users/{id}} — actualiza nombre/email. */
    public AppUserResponseDto update(Long id, UpdateAppUserRequestDto dto) {
        return client.put()
                .uri("/ifit/api/v1/users/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .body(AppUserResponseDto.class);
    }

    /** {@code DELETE /ifit/api/v1/users/{id}} — elimina el cliente en BD. */
    public void delete(Long id) {
        client.delete()
                .uri("/ifit/api/v1/users/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .retrieve()
                .toBodilessEntity();
    }

    private String bearer() {
        return "Bearer " + auth.getAccessToken();
    }
}
