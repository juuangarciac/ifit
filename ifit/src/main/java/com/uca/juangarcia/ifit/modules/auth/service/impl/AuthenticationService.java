package com.uca.juangarcia.ifit.modules.auth.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.uca.juangarcia.ifit.exception.EmailAlreadyExistsException;
import com.uca.juangarcia.ifit.exception.EmailNotFoundException;
import com.uca.juangarcia.ifit.exception.KeycloakUserCreationException;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.LoginResponseDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.RegisterRequestDTO;
import com.uca.juangarcia.ifit.modules.auth.controllers.dto.UserDTO;
import com.uca.juangarcia.ifit.modules.auth.service.IAuthenticationService;
import com.uca.juangarcia.ifit.modules.auth.service.IKeycloakService;
import com.uca.juangarcia.ifit.modules.user.dto.AppUserResponseDto;
import com.uca.juangarcia.ifit.modules.user.dto.CreateAppUserRequestDto;
import com.uca.juangarcia.ifit.modules.user.service.AppUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de autenticación que orquesta Keycloak y la base de datos de la aplicación.
 * 
 * <p>Proporciona operaciones de:
 * <ul>
 *   <li><strong>Login:</strong> Valida credenciales en Keycloak y devuelve tokens + perfil</li>
 *   <li><strong>Register:</strong> Crea usuario en Keycloak y BD de forma transaccional</li>
 *   <li><strong>Refresh:</strong> Renueva tokens usando refresh token válido</li>
 *   <li><strong>Logout:</strong> Invalida refresh token en Keycloak</li>
 * </ul>
 * 
 * <p><strong>Gestión de transacciones:</strong>
 * El registro es una operación transaccional que garantiza atomicidad:
 * - Si falla la creación en BD → Se elimina el usuario de Keycloak (rollback)
 * - Si falla Keycloak → No se crea en BD
 * 
 * @author Juan Garcia
 * @version 2.1
 * @since 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.token-url}")
    private String tokenUrl;
    
    @Value("${keycloak.logout-url}")
    private String logoutUrl;  // Nueva propiedad

    private final AppUserService appUserService;
    private final IKeycloakService keycloakService;
    private final RestTemplate restTemplate;

    /**
     * Autentica un usuario y devuelve tokens + perfil.
     * 
     * <p>Proceso de login:
     * <ol>
     *   <li>Valida credenciales contra Keycloak</li>
     *   <li>Obtiene accessToken y refreshToken</li>
     *   <li>Busca el perfil del usuario en la BD</li>
     *   <li>Devuelve respuesta unificada con tokens + datos del usuario</li>
     * </ol>
     * 
     * @param loginRequestDTO credenciales del usuario (email y password)
     * @return respuesta con tokens y perfil del usuario
     * @throws EmailNotFoundException si no se encuentra el perfil del usuario
     * @throws RuntimeException si las credenciales son inválidas o hay error de comunicación
     */
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) throws EmailNotFoundException {
        log.info("Attempting login for user: {}", loginRequestDTO.getUsername());
        
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
            // 1. Autenticar en Keycloak
            log.debug("Authenticating user in Keycloak: {}", loginRequestDTO.getUsername());
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody == null) {
                log.error("Empty response from Keycloak for user: {}", loginRequestDTO.getUsername());
                throw new RuntimeException("Empty response from authentication server");
            }

            log.info("Keycloak authentication successful for user: {}", loginRequestDTO.getUsername());

            // 2. Buscar perfil del usuario en BD
            log.debug("Fetching user profile from database: {}", loginRequestDTO.getUsername());
            AppUserResponseDto appUser = appUserService.findUserByEmail(loginRequestDTO.getUsername());

            log.info("User profile loaded successfully for: {}", loginRequestDTO.getUsername());

            // 3. Construir respuesta con tokens + perfil
            return LoginResponseDTO.builder()
                    .accessToken((String) responseBody.get("access_token"))
                    .refreshToken((String) responseBody.get("refresh_token"))
                    .expiresIn((Integer) responseBody.get("expires_in"))
                    .tokenType("Bearer")
                    .appUser(appUser)
                    .build();
            
        } catch (HttpClientErrorException e) {
            log.error("Login failed for user {}: {}", 
                     loginRequestDTO.getUsername(), 
                     e.getResponseBodyAsString());
            throw new RuntimeException("Invalid username or password");
        } catch (Exception e) {
            log.error("Unexpected error during login for user {}: {}", 
                     loginRequestDTO.getUsername(), 
                     e.getMessage(), e);
            throw new RuntimeException("Authentication service unavailable");
        }
    }

    /**
     * Registra un nuevo usuario en Keycloak y en la base de datos.
     * 
     * <p>Proceso de registro (transaccional):
     * <ol>
     *   <li>Verifica que el email no exista en BD</li>
     *   <li>Crea usuario en Keycloak → obtiene keycloakUserId</li>
     *   <li>Crea perfil en BD con referencia al keycloakUserId</li>
     *   <li>Realiza login automático</li>
     *   <li>Si algo falla → Rollback (elimina de Keycloak si fue creado)</li>
     * </ol>
     * 
     * <p><strong>Atomicidad garantizada:</strong>
     * La anotación @Transactional asegura que si falla la creación en BD,
     * se hace rollback eliminando el usuario de Keycloak.
     * 
     * @param registerDTO datos del nuevo usuario
     * @return respuesta con tokens y perfil del usuario registrado
     * @throws EmailAlreadyExistsException si el email ya está registrado
     * @throws RuntimeException si falla la creación en Keycloak o BD
     */
    @Override
    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO registerDTO) {
        log.info("Starting registration process for user: {}", registerDTO.getEmail());
        
        String keycloakUserId = null;

        try {
            // 1. Verificar que el email no exista en BD
            log.debug("Checking if email already exists: {}", registerDTO.getEmail());
            if (appUserService.existsByEmail(registerDTO.getEmail())) {
                log.error("Email already exists in database: {}", registerDTO.getEmail());
                throw new EmailAlreadyExistsException(registerDTO.getEmail());
            }

            // 2. Crear usuario en Keycloak
            log.info("Creating user in Keycloak: {}", registerDTO.getEmail());
            UserDTO keycloakUserDTO = UserDTO.builder()
                    .username(registerDTO.getEmail())
                    .email(registerDTO.getEmail())
                    .firstName(registerDTO.getName())
                    .lastName(registerDTO.getSurname())
                    .password(registerDTO.getPassword())
                    .build();

            keycloakUserId = keycloakService.createUser(keycloakUserDTO);
            log.info("User created in Keycloak with ID: {}", keycloakUserId);

            // 3. Crear perfil en BD con referencia a Keycloak
            log.info("Creating user profile in database for: {}", registerDTO.getEmail());
            
            // Construir nombre completo para el campo 'name'
            String fullName = registerDTO.getName() + 
                            (registerDTO.getSurname() != null && !registerDTO.getSurname().isEmpty() 
                                ? " " + registerDTO.getSurname() 
                                : "");
            
            CreateAppUserRequestDto createUserDto = new CreateAppUserRequestDto(
                    fullName,
                    registerDTO.getPassword(),
                    registerDTO.getEmail(),
                    keycloakUserId  // Guardar referencia a Keycloak
            );

            AppUserResponseDto createdUser = appUserService.createUser(createUserDto);
            log.info("User profile created successfully with ID: {}", createdUser.getId());

            // 4. Hacer login automático después del registro
            log.info("Performing automatic login for newly registered user: {}", registerDTO.getEmail());
            LoginRequestDTO loginRequest = new LoginRequestDTO(
                    registerDTO.getEmail(),
                    registerDTO.getPassword()
            );

            return login(loginRequest);

        } catch (KeycloakUserCreationException | EmailAlreadyExistsException e) {
            log.error("Registration failed for user {}: {}", registerDTO.getEmail(), e.getMessage());
            
            // Rollback: eliminar de Keycloak si se creó
            if (keycloakUserId != null) {
                log.warn("Rolling back - Deleting user from Keycloak: {}", keycloakUserId);
                try {
                    keycloakService.deleteUser(keycloakUserId);
                    log.info("Rollback successful - User deleted from Keycloak: {}", keycloakUserId);
                } catch (Exception deleteEx) {
                    log.error("Failed to rollback Keycloak user creation: {}", keycloakUserId, deleteEx);
                }
            }
            
            throw new RuntimeException("Registration failed: " + e.getMessage());
            
        } catch (Exception e) {
            log.error("Unexpected error during registration for user {}: {}", 
                     registerDTO.getEmail(), 
                     e.getMessage(), e);
            
            // Rollback: eliminar de Keycloak si se creó
            if (keycloakUserId != null) {
                log.warn("Rolling back - Deleting user from Keycloak: {}", keycloakUserId);
                try {
                    keycloakService.deleteUser(keycloakUserId);
                    log.info("Rollback successful - User deleted from Keycloak: {}", keycloakUserId);
                } catch (Exception deleteEx) {
                    log.error("Failed to rollback Keycloak user creation: {}", keycloakUserId, deleteEx);
                }
            }
            
            throw new RuntimeException("Registration service unavailable: " + e.getMessage());
        }
    }

    /**
     * Refresca los tokens usando un refresh token válido.
     * 
     * <p><strong>Diferencias con login():</strong>
     * <ul>
     *   <li>login() requiere email + password y consulta la BD</li>
     *   <li>refreshToken() requiere SOLO el refresh token y NO consulta BD</li>
     *   <li>refreshToken() es más rápido (no hay consulta a BD)</li>
     * </ul>
     * 
     * <p><strong>Proceso:</strong>
     * <ol>
     *   <li>Envía refresh token a Keycloak con grant_type="refresh_token"</li>
     *   <li>Keycloak valida el refresh token</li>
     *   <li>Si es válido, genera NUEVOS access y refresh tokens</li>
     *   <li>Devuelve los nuevos tokens (sin datos de usuario)</li>
     * </ol>
     * 
     * @param refreshToken el refresh token actual
     * @return respuesta con NUEVOS tokens
     * @throws RuntimeException si el refresh token es inválido o expiró
     */
    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        log.info("Refreshing authentication tokens");
        
        // Construir request para Keycloak
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");  
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken); 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // Llamar a Keycloak para obtener nuevos tokens
            log.debug("Requesting new tokens from Keycloak");
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody == null) {
                log.error("Empty response from Keycloak during token refresh");
                throw new RuntimeException("Empty response from authentication server");
            }

            log.info("Tokens refreshed successfully");

            // Construir respuesta con nuevos tokens
            // NOTA: No incluimos appUser porque no hacemos consulta a BD
            return LoginResponseDTO.builder()
                    .accessToken((String) responseBody.get("access_token"))
                    .refreshToken((String) responseBody.get("refresh_token"))
                    .expiresIn((Integer) responseBody.get("expires_in"))
                    .tokenType("Bearer")
                    .appUser(null) 
                    .build();
            
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Refresh token invalid or expired: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Refresh token invalid or expired");
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage(), e);
            throw new RuntimeException("Token refresh service unavailable");
        }
    }

    /**
     * Cierra la sesión del usuario invalidando su refresh token en Keycloak.
     * 
     * <p><strong>Proceso:</strong>
     * <ol>
     *   <li>Envía petición de logout a Keycloak</li>
     *   <li>Keycloak invalida el refresh token</li>
     *   <li>Los access tokens derivados dejan de ser válidos</li>
     * </ol>
     * 
     * <p><strong>Importante:</strong>
     * El cliente también debe eliminar los tokens de su almacenamiento local
     * (SecureStorage, SharedPreferences, etc.) después de llamar a este endpoint.
     * 
     * @param refreshToken el refresh token a invalidar
     * @throws RuntimeException si hay error al comunicarse con Keycloak
     */
    @Override
    public void logout(String refreshToken) {
        log.info("Processing user logout");
        
        // Construir request para logout de Keycloak
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            // Llamar a Keycloak para invalidar el refresh token
            log.debug("Invalidating refresh token in Keycloak");
            restTemplate.postForEntity(logoutUrl, request, String.class);
            
            log.info("User logged out successfully - Refresh token invalidated");
            
        } catch (HttpClientErrorException e) {
            log.error("Logout failed: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Logout failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during logout: {}", e.getMessage(), e);
            throw new RuntimeException("Logout service unavailable");
        }
    }
}