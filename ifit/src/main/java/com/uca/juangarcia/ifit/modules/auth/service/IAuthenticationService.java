package com.uca.juangarcia.ifit.modules.auth.service;

import org.springframework.security.authentication.AuthenticationServiceException;

import com.uca.juangarcia.ifit.exception.dto.EmailAlreadyExistsException;
import com.uca.juangarcia.ifit.exception.dto.EmailNotFoundException;
import com.uca.juangarcia.ifit.exception.dto.InvalidCredentialsException;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginResponseDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.RegisterRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.RegisterResponseDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.VerifyUserRequestDTO;

/**
 * Interfaz de servicio de autenticación.
 * 
 * <p>Define las operaciones principales de autenticación:
 * <ul>
 *   <li>Login de usuarios existentes</li>
 *   <li>Registro de nuevos usuarios</li>
 *   <li>Refresh de tokens (renovación)</li>
 *   <li>Logout (invalidación de tokens)</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 2.1
 * @since 1.0
 */
public interface IAuthenticationService {
    
    
    /**
     * Autentica a un usuario con email y contraseña.
     * 
     * <p>Proceso:
     * <ol>
     *   <li>Verifica que el email exista en la base de datos</li>
     *   <li>Valida las credenciales con Keycloak</li>
     *   <li>Genera tokens de acceso y refresh</li>
     *   <li>Devuelve la respuesta con los tokens y perfil del usuario</li>
     * </ol>
     * 
     * @param loginRequestDTO datos de login (email y password)
     * @return respuesta con tokens y perfil del usuario
     * @throws EmailNotFoundException si el email no existe en la BD
     * @throws InvalidCredentialsException si las credenciales son inválidas
     * @throws AuthenticationServiceException si hay error en el servicio de autenticación
     */
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO) throws InvalidCredentialsException, AuthenticationServiceException;
    
    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * <p>Crea el usuario tanto en Keycloak como en la base de datos
     * de forma transaccional.
     * 
     * @param registerDTO datos del nuevo usuario
     * @return respuesta con tokens y perfil del usuario creado
     * @throws EmailAlreadyExistsException 
     */
    RegisterResponseDTO register(RegisterRequestDTO registerDTO) throws EmailAlreadyExistsException;
    
    /**
     * Refresca los tokens de autenticación usando un refresh token válido.
     * 
     * <p>Proceso:
     * <ol>
     *   <li>Valida el refresh token con Keycloak</li>
     *   <li>Genera NUEVOS access token y refresh token</li>
     *   <li>Devuelve la respuesta con los nuevos tokens</li>
     * </ol>
     * 
     * <p><strong>Diferencias con login():</strong>
     * <ul>
     *   <li>login() requiere email + password</li>
     *   <li>refreshToken() requiere solo el refresh token</li>
     *   <li>refreshToken() NO consulta la BD (más rápido)</li>
     * </ul>
     * 
     * <p><strong>Cuándo usar:</strong>
     * Cuando el access token expira (típicamente cada 5-15 minutos)
     * para evitar que el usuario tenga que hacer login de nuevo.
     * 
     * @param refreshToken el refresh token actual
     * @return respuesta con NUEVOS tokens (access y refresh)
     * @throws RuntimeException si el refresh token es inválido o expiró
     */
    LoginResponseDTO refreshToken(String refreshToken);
    
    /**
     * Cierra la sesión del usuario invalidando su refresh token en Keycloak.
     * 
     * <p>Proceso:
     * <ol>
     *   <li>Envía petición de logout a Keycloak</li>
     *   <li>Keycloak invalida el refresh token</li>
     *   <li>Los access tokens existentes dejan de ser válidos</li>
     * </ol>
     * 
     * <p><strong>Importante:</strong>
     * El cliente también debe eliminar los tokens localmente
     * (SecureStorage o similar) después de llamar a este endpoint.
     * 
     * @param refreshToken el refresh token a invalidar
     * @throws RuntimeException si hay error al comunicarse con Keycloak
     */
    void logout(String refreshToken);

    /**
     * Verifica el email del usuario usando un código de verificación.
     * 
     * @param request datos de verificación (email y código)
     * @return respuesta de login con tokens si la verificación es exitosa
     */
    LoginResponseDTO verifyEmail(VerifyUserRequestDTO request) throws IllegalArgumentException, EmailNotFoundException, InvalidCredentialsException;
}