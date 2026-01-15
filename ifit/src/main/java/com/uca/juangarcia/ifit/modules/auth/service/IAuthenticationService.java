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
 * </ul>
 * 
 * @author Juan Garcia
 * @version 2.0
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
}