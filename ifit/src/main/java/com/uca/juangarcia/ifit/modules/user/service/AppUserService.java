package com.uca.juangarcia.ifit.modules.user.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uca.juangarcia.ifit.exception.CoachModelTypeNotFoundException;
import com.uca.juangarcia.ifit.exception.EmailAlreadyExistsException;
import com.uca.juangarcia.ifit.exception.EmailNotFoundException;
import com.uca.juangarcia.ifit.exception.ExperienceLevelNotFoundException;
import com.uca.juangarcia.ifit.exception.UserIdNotFoundException;
import com.uca.juangarcia.ifit.modules.coach.mapper.CoachModelTypeMapper;
import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;
import com.uca.juangarcia.ifit.modules.coach.service.CoachModelTypeService;
import com.uca.juangarcia.ifit.modules.user.dto.AppUserResponseDto;
import com.uca.juangarcia.ifit.modules.user.dto.CreateAppUserRequestDto;
import com.uca.juangarcia.ifit.modules.user.dto.ExperienceLevelDto;
import com.uca.juangarcia.ifit.modules.user.dto.UpdateAppUserRequestDto;
import com.uca.juangarcia.ifit.modules.user.mapper.AppUserMapper;
import com.uca.juangarcia.ifit.modules.user.mapper.ExperienceLevelMapper;
import com.uca.juangarcia.ifit.modules.user.model.AppRole;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;
import com.uca.juangarcia.ifit.modules.user.model.ExperienceLevel;
import com.uca.juangarcia.ifit.modules.user.repository.AppUserRepository;

/**
 * Servicio para la gestión de usuarios de la aplicación.
 * 
 * <p>Este servicio proporciona operaciones CRUD y funcionalidades específicas
 * para gestionar usuarios, incluyendo:
 * <ul>
 *   <li>Creación y registro de nuevos usuarios</li>
 *   <li>Búsqueda y consulta de usuarios</li>
 *   <li>Actualización de información de usuario</li>
 *   <li>Asignación de roles, coaches y niveles de experiencia</li>
 *   <li>Gestión del proceso de registro y verificación</li>
 * </ul>
 * 
 * <p>Todas las operaciones de escritura se ejecutan dentro de transacciones
 * para garantizar la integridad de los datos.
 * 
 * @author Juan Garcia
 * @version 2.0
 * @since 1.0
 */
@Service
@Transactional(readOnly = true)
public class AppUserService {

    private static final Logger logger = LoggerFactory.getLogger(AppUserService.class);

    private final AppUserRepository userRepository;
    private final AppRoleService appRoleService;
    private final CoachModelTypeService coachModelTypeService;
    private final ExperienceLevelService experienceLevelService;
    private final AppUserMapper userMapper;
    private final CoachModelTypeMapper coachModelTypeMapper;
    private final ExperienceLevelMapper experienceLevelMapper;
    private final PasswordEncoder passwordEncoder;


    @Value("${ifit.default.user.role}")
    private String defaultUserRole;

    /**
     * Constructor con inyección de dependencias.
     * 
     * <p>Se utiliza inyección por constructor en lugar de @Autowired en campos
     * para facilitar el testing y hacer explícitas las dependencias.
     * 
     * @param userRepository repositorio de usuarios
     * @param appRoleService servicio de roles
     * @param coachModelTypeService servicio de tipos de coach
     * @param experienceLevelService servicio de niveles de experiencia
     * @param userMapper mapper para conversión de DTOs
     * @param coachModelTypeMapper mapper para tipos de coach
     * @param passwordEncoder encoder para encriptar contraseñas
     */
    public AppUserService(
            AppUserRepository userRepository,
            AppRoleService appRoleService,
            CoachModelTypeService coachModelTypeService,
            ExperienceLevelService experienceLevelService,
            AppUserMapper userMapper,
            CoachModelTypeMapper coachModelTypeMapper,
            ExperienceLevelMapper experienceLevelMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.appRoleService = appRoleService;
        this.coachModelTypeService = coachModelTypeService;
        this.experienceLevelService = experienceLevelService;
        this.userMapper = userMapper;
        this.coachModelTypeMapper = coachModelTypeMapper;
        this.experienceLevelMapper = experienceLevelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * <p>Este método devuelve una lista completa de todos los usuarios registrados.
     * Para conjuntos grandes de datos, se recomienda usar {@link #findAllUsers(Pageable)}
     * que permite paginación.
     * 
     * @return lista de DTOs de respuesta con todos los usuarios
     * @throws IllegalStateException si no hay usuarios en el sistema
     * 
     * @see #findAllUsers(Pageable)
     */
    public List<AppUserResponseDto> findAllUsers() {
        logger.debug("Finding all users");
        
        List<AppUser> users = userRepository.findAll();
        
        if (users.isEmpty()) {
            logger.warn("No users found in database");
            throw new IllegalStateException("No users found in the system");
        }

        logger.info("Found {} users", users.size());
        return users.stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    /**
     * Obtiene usuarios con paginación.
     * 
     * <p>Este método es preferible a {@link #findAllUsers()} cuando se trabaja
     * con grandes volúmenes de datos, ya que permite obtener los resultados
     * en páginas controladas.
     * 
     * <p><strong>Ejemplo de uso:</strong>
     * <pre>
     * Pageable pageable = PageRequest.of(0, 20); // Primera página, 20 elementos
     * Page&lt;AppUserResponseDto&gt; users = userService.findAllUsers(pageable);
     * </pre>
     * 
     * @param pageable configuración de paginación (página, tamaño, ordenamiento)
     * @return página de DTOs de respuesta con los usuarios
     */
    public Page<AppUserResponseDto> findAllUsers(Pageable pageable) {
        logger.debug("Finding users with pagination: page={}, size={}", 
                    pageable.getPageNumber(), pageable.getPageSize());
        
        Page<AppUser> usersPage = userRepository.findAll(pageable);
        
        logger.info("Found {} users in page {} of {}", 
                   usersPage.getNumberOfElements(), 
                   usersPage.getNumber(), 
                   usersPage.getTotalPages());
        
        return usersPage.map(userMapper::toResponseDto);
    }

    /**
     * Busca un usuario por su ID.
     * 
     * @param id identificador único del usuario
     * @return DTO de respuesta con los datos del usuario
     * @throws UserIdNotFoundException si no existe un usuario con el ID especificado
     * @throws IllegalArgumentException si el ID es nulo
     */
    public AppUserResponseDto findUserById(Long id) throws UserIdNotFoundException {
        if (id == null) {
            logger.error("Attempted to find user with null ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        logger.debug("Finding user by ID: {}", id);
        
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", id);
                    return new UserIdNotFoundException(id);
                });

        logger.info("Found user: {} ({})", user.getName(), user.getEmail());
        return userMapper.toResponseDto(user);
    }

    /**
     * Busca un usuario por su dirección de email.
     * 
     * @param email dirección de email del usuario
     * @return DTO de respuesta con los datos del usuario
     * @throws EmailNotFoundException si no existe un usuario con el email especificado
     * @throws IllegalArgumentException si el email es nulo o vacío
     */
    public AppUserResponseDto findUserByEmail(String email) throws EmailNotFoundException {
        if (email == null || email.isBlank()) {
            logger.error("Attempted to find user with null or blank email");
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        logger.debug("Finding user by email: {}", email);
        
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", email);
                    return new EmailNotFoundException(email);
                });

        logger.info("Found user: {} with email: {}", user.getName(), email);
        return userMapper.toResponseDto(user);
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * <p>Este método realiza las siguientes operaciones:
     * <ol>
     *   <li>Valida que el email no esté ya registrado</li>
     *   <li>Encripta la contraseña proporcionada</li>
     *   <li>Asigna el rol predeterminado del sistema</li>
     *   <li>Establece valores iniciales (verificación pendiente, registro incompleto)</li>
     *   <li>Guarda el usuario en la base de datos</li>
     * </ol>
     * 
     * @param createDto DTO con los datos del nuevo usuario
     * @return DTO de respuesta con los datos del usuario creado (incluyendo ID generado)
     * @throws EmailAlreadyExistsException si ya existe un usuario con el email proporcionado
     * @throws IllegalArgumentException si el DTO es nulo
     * @throws IllegalStateException si no se encuentra el rol predeterminado
     */
    @Transactional
    public AppUserResponseDto createUser(CreateAppUserRequestDto createDto) throws EmailAlreadyExistsException {
        if (createDto == null) {
            logger.error("Attempted to create user with null DTO");
            throw new IllegalArgumentException("CreateAppUserRequestDto cannot be null");
        }

        logger.debug("Creating new user with email: {}", createDto.email());

        // Verificar que el email no esté ya registrado
        if (userRepository.findByEmail(createDto.email()).isPresent()) {
            logger.error("Email already exists: {}", createDto.email());
            throw new EmailAlreadyExistsException(createDto.email());
        }

        // Convertir DTO a entidad
        AppUser user = userMapper.toEntity(createDto);

        // Encriptar contraseña
        String encryptedPassword = passwordEncoder.encode(createDto.password());
        user.setPassword(encryptedPassword);

        // Asignar rol predeterminado
        try {
            AppRole defaultRole = appRoleService.findRoleByName(defaultUserRole);
            user.setRole(defaultRole);
        } catch (Exception e) {
            logger.error("Failed to assign default role '{}' to user", defaultUserRole, e);
            throw new IllegalStateException("Could not assign default role to user", e);
        }

        // Guardar usuario
        AppUser savedUser = userRepository.save(user);
        
        logger.info("User created successfully: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        
        return userMapper.toResponseDto(savedUser);
    }

    /**
     * Actualiza los datos básicos de un usuario existente.
     * 
     * <p>Este método permite actualizaciones parciales. Solo los campos no nulos
     * en el DTO serán actualizados en la entidad.
     * 
     * @param id identificador del usuario a actualizar
     * @param updateDto DTO con los nuevos datos del usuario
     * @return DTO de respuesta con los datos actualizados del usuario
     * @throws UserIdNotFoundException si no existe un usuario con el ID especificado
     * @throws EmailAlreadyExistsException si el nuevo email ya está registrado por otro usuario
     * @throws IllegalArgumentException si algún parámetro es nulo
     */
    @Transactional
    public AppUserResponseDto updateUser(Long id, UpdateAppUserRequestDto updateDto) throws UserIdNotFoundException, EmailAlreadyExistsException {
        if (id == null) {
            logger.error("Attempted to update user with null ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (updateDto == null) {
            logger.error("Attempted to update user with null DTO");
            throw new IllegalArgumentException("UpdateAppUserRequestDto cannot be null");
        }

        logger.debug("Updating user with ID: {}", id);

        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found for update with ID: {}", id);
                    return new UserIdNotFoundException(id);
                });

        // Verificar si el nuevo email ya existe (solo si se está cambiando)
        if (updateDto.email() != null && !updateDto.email().equals(user.getEmail())) {
            if (userRepository.findByEmail(updateDto.email()).isPresent()) {
                logger.error("Cannot update user: email {} already exists", updateDto.email());
                throw new EmailAlreadyExistsException(updateDto.email());
            }
        }

        // Actualizar campos
        userMapper.updateEntityFromDto(user, updateDto);
        
        AppUser updatedUser = userRepository.save(user);
        
        logger.info("User updated successfully: {} (ID: {})", updatedUser.getEmail(), updatedUser.getId());
        
        return userMapper.toResponseDto(updatedUser);
    }

    /**
     * Asigna un tipo de coach a un usuario.
     * 
     * <p>El tipo de coach determina el modelo de IA que asistirá al usuario
     * en su entrenamiento.
     * 
     * @param userId identificador del usuario
     * @param coachId identificador del tipo de coach
     * @return DTO de respuesta con los datos actualizados del usuario
     * @throws UserIdNotFoundException si no existe el usuario
     * @throws CoachModelTypeNotFoundException si no existe el tipo de coach
     * @throws IllegalArgumentException si algún parámetro es nulo
     */
    @Transactional
    public AppUserResponseDto setCoachModelType(Long userId, Long coachId) throws UserIdNotFoundException, CoachModelTypeNotFoundException {
        if (userId == null) {
            logger.error("Attempted to set coach with null user ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (coachId == null) {
            logger.error("Attempted to set coach with null coach ID");
            throw new IllegalArgumentException("Coach ID cannot be null");
        }

        logger.debug("Setting coach model type {} for user {}", coachId, userId);

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new UserIdNotFoundException(userId);
                });

        CoachModelType coach = coachModelTypeMapper.toEntity(coachModelTypeService.getById(coachId));

        user.setCoachModelType(coach);
        AppUser updatedUser = userRepository.save(user);

        logger.info("Coach model type '{}' assigned to user {}", 
                   coach.getName(), user.getEmail());

        return userMapper.toResponseDto(updatedUser);
    }

    /**
     * Asigna un nivel de experiencia a un usuario.
     * 
     * <p>El nivel de experiencia se utiliza para personalizar los entrenamientos
     * según las capacidades del usuario.
     * 
     * @param userId identificador del usuario
     * @param experienceLevelId identificador del nivel de experiencia
     * @return DTO de respuesta con los datos actualizados del usuario
     * @throws UserIdNotFoundException si no existe el usuario
     * @throws ExperienceLevelNotFoundException si no existe el nivel de experiencia
     * @throws IllegalArgumentException si algún parámetro es nulo
     */
    @Transactional
    public AppUserResponseDto setExperienceLevel(Long userId, Long experienceLevelId) throws UserIdNotFoundException, ExperienceLevelNotFoundException {
        if (userId == null) {
            logger.error("Attempted to set experience level with null user ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (experienceLevelId == null) {
            logger.error("Attempted to set experience level with null experience level ID");
            throw new IllegalArgumentException("Experience Level ID cannot be null");
        }

        logger.debug("Setting experience level {} for user {}", experienceLevelId, userId);

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new UserIdNotFoundException(userId);
                });

        ExperienceLevelDto experienceLevelDto = experienceLevelService.getById(experienceLevelId);
        ExperienceLevel experienceLevel = experienceLevelMapper.toEntity(experienceLevelDto);
        
        user.setExperienceLevel(experienceLevel);
        AppUser updatedUser = userRepository.save(user);

        logger.info("Experience level '{}' assigned to user {}", 
                   experienceLevel.getName(), user.getEmail());

        return userMapper.toResponseDto(updatedUser);
    }

    /**
     * Marca el proceso de registro de un usuario como completado.
     * 
     * <p>Este método se invoca cuando el usuario ha completado todos los pasos
     * del proceso de onboarding (selección de coach, nivel de experiencia,
     * cuestionario inicial, etc.).
     * 
     * @param userId identificador del usuario
     * @return DTO de respuesta con los datos actualizados del usuario
     * @throws UserIdNotFoundException si no existe el usuario
     * @throws IllegalArgumentException si el ID es nulo
     */
    @Transactional
    public AppUserResponseDto markRegistrationComplete(Long userId) throws UserIdNotFoundException {
        if (userId == null) {
            logger.error("Attempted to mark registration complete with null user ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        logger.debug("Marking registration as complete for user {}", userId);

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new UserIdNotFoundException(userId);
                });

        user.setIsRegistrationComplete(true);
        AppUser updatedUser = userRepository.save(user);

        logger.info("Registration marked as complete for user: {}", user.getEmail());

        return userMapper.toResponseDto(updatedUser);
    }

    /**
     * Elimina un usuario del sistema.
     * 
     * <p><strong>Advertencia:</strong> Esta operación es irreversible y eliminará
     * permanentemente al usuario y todas sus referencias en cascada.
     * 
     * @param userId identificador del usuario a eliminar
     * @throws UserIdNotFoundException si no existe el usuario
     * @throws IllegalArgumentException si el ID es nulo
     */
    @Transactional
    public void deleteUser(Long userId) throws UserIdNotFoundException {
        if (userId == null) {
            logger.error("Attempted to delete user with null ID");
            throw new IllegalArgumentException("User ID cannot be null");
        }

        logger.debug("Deleting user with ID: {}", userId);

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found for deletion with ID: {}", userId);
                    return new UserIdNotFoundException(userId);
                });

        userRepository.delete(user);

        logger.info("User deleted successfully: {} (ID: {})", user.getEmail(), userId);
    }

    /**
     * Verifica si existe un usuario con el email especificado.
     * 
     * @param email email a verificar
     * @return true si existe un usuario con ese email, false en caso contrario
     * @throws IllegalArgumentException si el email es nulo o vacío
     */
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        return userRepository.findByEmail(email).isPresent();
    }
}
