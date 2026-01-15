package com.uca.juangarcia.ifit.modules.auth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uca.juangarcia.ifit.exception.EmailNotFoundException;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginResponseDTO;
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
 * </ul>
 * 
 * <p>Todas las respuestas incluyen tokens de Keycloak y perfil completo del usuario.
 * 
 * @author Juan Garcia
 * @version 2.0
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para autenticación y registro de usuarios")
public class AuthenticationController {
    
    private final IAuthenticationService authenticationService;

    /**
     * Autentica un usuario existente.
     * 
     * <p>Valida las credenciales contra Keycloak y devuelve:
     * <ul>
     *   <li>Access token (JWT)</li>
     *   <li>Refresh token</li>
     *   <li>Tiempo de expiración</li>
     *   <li>Perfil completo del usuario</li>
     * </ul>
     * 
     * @param loginRequestDTO credenciales del usuario (email y password)
     * @return respuesta con tokens y perfil del usuario
     * @throws EmailNotFoundException si no se encuentra el perfil del usuario
     */
    @Operation(
        summary = "Login de usuario",
        description = "Autentica un usuario existente y devuelve tokens de acceso junto con su perfil"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login exitoso"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) 
            throws EmailNotFoundException {
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
        description = "Crea un nuevo usuario en Keycloak y en la base de datos, luego realiza login automático"
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
}