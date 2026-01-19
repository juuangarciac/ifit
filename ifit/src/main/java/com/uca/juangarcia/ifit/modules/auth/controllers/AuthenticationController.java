package com.uca.juangarcia.ifit.modules.auth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uca.juangarcia.ifit.exception.dto.EmailNotFoundException;
import com.uca.juangarcia.ifit.exception.dto.InvalidCredentialsException;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginResponseDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LogoutResponseDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.RefreshTokenRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.RegisterRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.service.IAuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controlador REST para operaciones de autenticación.
 * 
 * <p>Proporciona endpoints para:
 * <ul>
 *   <li>Login de usuarios existentes</li>
 *   <li>Registro de nuevos usuarios</li>
 *   <li>Refresh de tokens (renovación automática)</li>
 *   <li>Logout (invalidación de tokens)</li>
 * </ul>
 * 
 * <p>Todas las respuestas de login/register/refresh incluyen tokens JWT de Keycloak.
 * 
 * @author Juan Garcia
 * @version 2.1
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para autenticación y gestión de tokens JWT")
public class AuthenticationController {
    
    private final IAuthenticationService authenticationService;

    /**
     * Autentica un usuario existente.
     * 
     * <p>Valida las credenciales contra Keycloak y devuelve:
     * <ul>
     *   <li>Access token (JWT) - Válido por ~5 minutos</li>
     *   <li>Refresh token - Válido por ~30 días</li>
     *   <li>Tiempo de expiración del access token</li>
     *   <li>Perfil completo del usuario</li>
     * </ul>
     * 
     * <p><strong>Cuándo usar:</strong>
     * <ul>
     *   <li>Primera vez que el usuario entra a la app</li>
     *   <li>Después de hacer logout</li>
     *   <li>Cuando el refresh token ha expirado</li>
     * </ul>
     * 
     * @param loginRequestDTO credenciales del usuario (email y password)
     * @return respuesta con tokens y perfil del usuario
     * @throws InvalidCredentialsException si las credenciales son inválidas
     * @throws AuthenticationServiceException si hay error en el servicio de autenticación
     */
    @Operation(
        summary = "Login de usuario",
        description = "Autentica un usuario con email y contraseña. Retorna access token (5 min), refresh token (30 días) y perfil completo del usuario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) 
            throws InvalidCredentialsException, AuthenticationServiceException {
        log.info("Login request received for: {}", loginRequestDTO.getUsername());
        LoginResponseDTO response = authenticationService.login(loginRequestDTO);
        log.info("Login successful for: {}", loginRequestDTO.getUsername());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * <p>Proceso de registro:
     * <ol>
     *   <li>Crea usuario en Keycloak</li>
     *   <li>Crea perfil en base de datos</li>
     *   <li>Realiza login automático</li>
     * </ol>
     * 
     * <p>En caso de error, se hace rollback automático en Keycloak.
     * 
     * @param registerDTO datos del nuevo usuario
     * @return respuesta con tokens y perfil del usuario creado (código 201)
     */
    @Operation(
        summary = "Registro de nuevo usuario",
        description = "Crea un nuevo usuario en Keycloak y en la base de datos, luego realiza login automático. Si falla, hace rollback completo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El email ya está registrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerDTO) {
        log.info("Registration request received for: {}", registerDTO.getEmail());
        LoginResponseDTO response = authenticationService.register(registerDTO);
        log.info("Registration successful for: {}", registerDTO.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Refresca los tokens de autenticación usando un refresh token válido.
     * 
     * @param request objeto con el refreshToken actual
     * @return respuesta con NUEVOS access token y refresh token
     */
    @Operation(
        summary = "Refrescar tokens JWT",
        description = "Obtiene nuevos access token y refresh token usando un refresh token válido. "
                    + "NO requiere email ni password. Más rápido que login porque no consulta la base de datos. "
                    + "Usar cuando el access token expira (típicamente cada 5-15 minutos)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tokens refrescados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Refresh token no proporcionado"),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        log.info("Token refresh request received");
        LoginResponseDTO response = authenticationService.refreshToken(request.getRefreshToken());
        log.info("Tokens refreshed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Cierra la sesión del usuario invalidando su refresh token en Keycloak.
     * 
     * <p><strong>Proceso:</strong>
     * <ol>
     *   <li>Invalida el refresh token en Keycloak</li>
     *   <li>Los access tokens derivados dejan de ser válidos</li>
     *   <li>El cliente debe eliminar los tokens de su almacenamiento local</li>
     * </ol>
     * 
     * <p><strong>Importante para el cliente (.NET MAUI):</strong>
     * Después de llamar a este endpoint, el cliente debe:
     * <pre>
     * 1. Llamar a /auth/logout (este endpoint)
     * 2. Eliminar tokens del SecureStorage local
     * 3. Redirigir al usuario a la pantalla de login
     * </pre>
     * @param request objeto con el refreshToken a invalidar
     * @return mensaje de confirmación (código 200)
     */
    @Operation(
        summary = "Cerrar sesión",
        description = "Invalida el refresh token en Keycloak para cerrar la sesión del usuario. "
                    + "El cliente debe eliminar los tokens de su almacenamiento local después de llamar a este endpoint."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Refresh token no proporcionado"),
        @ApiResponse(responseCode = "500", description = "Error al comunicarse con Keycloak")
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(@Valid @RequestBody RefreshTokenRequestDTO request) {
        log.info("Logout request received");
        authenticationService.logout(request.getRefreshToken());
        log.info("Logout successful - Refresh token invalidated");
        
        return ResponseEntity.ok(
            LogoutResponseDTO.builder()
            .message("Session closed successfully")
            .success(true)
            .build());
    }
}