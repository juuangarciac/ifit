package com.uca.juangarcia.ifit.modules.auth.service;

import com.uca.juangarcia.ifit.exception.EmailNotFoundException;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginResponseDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.RegisterRequestDTO;

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
     * Autentica un usuario y devuelve tokens + perfil.
     * 
     * @param loginRequestDTO credenciales del usuario
     * @return respuesta con tokens y perfil del usuario
     * @throws EmailNotFoundException si no se encuentra el perfil del usuario
     */
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO) throws EmailNotFoundException;
    
    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * <p>Crea el usuario tanto en Keycloak como en la base de datos
     * de forma transaccional.
     * 
     * @param registerDTO datos del nuevo usuario
     * @return respuesta con tokens y perfil del usuario creado
     */
    LoginResponseDTO register(RegisterRequestDTO registerDTO);
    
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
}