package com.ifit.ronnie.configuration;

import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

public class JwtUtils {

    private JwtUtils() {}

    public static String extractUserId(HttpServletRequest request, ObjectMapper objectMapper) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("[JwtUtils] Authorization header ausente o mal formado: " + authHeader);
                return null;
            }
            String payload = authHeader.substring(7).split("\\.")[1];
            Map<String, Object> claims = objectMapper.readValue(
                Base64.getUrlDecoder().decode(payload),
                new TypeReference<Map<String, Object>>() {}
            );
            String userId = (String) claims.get("sub");
            System.out.println("[JwtUtils] userId extraído del JWT: " + userId);
            return userId;
        } catch (Exception e) {
            System.out.println("[JwtUtils] Error al extraer userId del JWT: " + e.getMessage());
            return null;
        }
    }
}
