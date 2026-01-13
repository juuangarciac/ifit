package com.uca.juangarcia.ifit.modules.auth.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginResponseDTO;
import com.uca.juangarcia.ifit.modules.auth.service.IAuthenticationService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthenticationService implements IAuthenticationService {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.token-url}")
    private String tokenUrl;

    private final RestTemplate restTemplate;

    public AuthenticationService() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", loginRequestDTO.getUsername());
        body.add("password", loginRequestDTO.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody == null) {
                throw new RuntimeException("Empty response from authentication server");
            }

            return new LoginResponseDTO(
                (String) responseBody.get("access_token"),
                (String) responseBody.get("refresh_token"),
                (Integer) responseBody.get("expires_in")
            );
        } catch (HttpClientErrorException e) {
            log.error("Login failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Invalid username or password");
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage());
            throw new RuntimeException("Authentication service unavailable");
        }
    }
}