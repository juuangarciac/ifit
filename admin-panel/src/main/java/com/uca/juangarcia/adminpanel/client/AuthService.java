package com.uca.juangarcia.adminpanel.client;

import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uca.juangarcia.adminpanel.dto.AppUserResponseDto;
import com.uca.juangarcia.adminpanel.dto.LoginRequestDto;
import com.uca.juangarcia.adminpanel.dto.LoginResponseDto;
import com.vaadin.flow.server.VaadinSession;

/**
 * Servicio de autenticación del panel.
 *
 * <p>Hace login contra el Gateway ({@code /ifit/api/v1/auth/login}), <strong>verifica que el token
 * contenga la resource role {@code admin_client_role}</strong> del cliente {@code springboot-ifit-client}
 * y guarda los tokens + perfil en la {@link VaadinSession}. El backend además exige ese rol en cada
 * endpoint administrativo (defensa en profundidad).
 *
 * @author Juan Garcia
 * @version 1.0
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final String ATTR_TOKEN = "ifit.accessToken";
    private static final String ATTR_REFRESH = "ifit.refreshToken";
    private static final String ATTR_USER = "ifit.appUser";

    private static final String RESOURCE_ID = "springboot-ifit-client";
    private static final String ADMIN_ROLE = "admin_client_role";

    private final RestClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthService(RestClient gatewayRestClient) {
        this.client = gatewayRestClient;
    }

    /**
     * Intenta autenticar y autorizar como administrador.
     *
     * @return {@link Optional#empty()} si el login es correcto; o un mensaje de error para mostrar.
     */
    public Optional<String> login(String username, String password) {
        LoginResponseDto resp;
        try {
            resp = client.post()
                    .uri("/ifit/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new LoginRequestDto(username, password))
                    .retrieve()
                    .body(LoginResponseDto.class);
        } catch (RestClientResponseException ex) {
            log.warn("Login rechazado por el servidor ({}) para {}", ex.getStatusCode(), username);
            return Optional.of("Credenciales inválidas.");
        } catch (Exception ex) {
            log.error("No se pudo contactar con el Gateway: {}", ex.getMessage());
            return Optional.of("No se puede contactar con el servidor. ¿Está el Gateway (:8080) levantado?");
        }

        if (resp == null || resp.accessToken() == null) {
            return Optional.of("La cuenta no está verificada o el servidor no devolvió token.");
        }
        if (!tokenHasAdminRole(resp.accessToken())) {
            return Optional.of("La cuenta no tiene permisos de administrador.");
        }

        VaadinSession session = VaadinSession.getCurrent();
        session.setAttribute(ATTR_TOKEN, resp.accessToken());
        session.setAttribute(ATTR_REFRESH, resp.refreshToken());
        session.setAttribute(ATTR_USER, resp.appUser());
        log.info("Admin autenticado: {}", username);
        return Optional.empty();
    }

    /** Limpia la sesión (logout local; los tokens de Keycloak expiran por su cuenta). */
    public void logout() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(ATTR_TOKEN, null);
            session.setAttribute(ATTR_REFRESH, null);
            session.setAttribute(ATTR_USER, null);
        }
    }

    public boolean isAuthenticated() {
        return getAccessToken() != null;
    }

    public String getAccessToken() {
        VaadinSession session = VaadinSession.getCurrent();
        return session == null ? null : (String) session.getAttribute(ATTR_TOKEN);
    }

    public AppUserResponseDto getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        return session == null ? null : (AppUserResponseDto) session.getAttribute(ATTR_USER);
    }

    /**
     * Comprueba si el JWT contiene {@code resource_access.springboot-ifit-client.roles[admin_client_role]}.
     * Decodifica el payload (segunda parte del JWT) sin validar la firma — la validación criptográfica
     * la hace el Gateway/iFit en cada llamada; aquí solo es un gate de UX.
     */
    private boolean tokenHasAdminRole(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                return false;
            }
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode roles = objectMapper.readTree(payload)
                    .path("resource_access").path(RESOURCE_ID).path("roles");
            if (roles.isArray()) {
                for (JsonNode role : roles) {
                    if (ADMIN_ROLE.equals(role.asText())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            log.warn("No se pudo decodificar el JWT para comprobar el rol admin: {}", e.getMessage());
            return false;
        }
    }
}
