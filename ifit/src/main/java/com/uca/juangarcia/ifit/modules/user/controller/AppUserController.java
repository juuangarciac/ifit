package com.uca.juangarcia.ifit.modules.user.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uca.juangarcia.ifit.exception.CoachModelTypeNotFoundException;
import com.uca.juangarcia.ifit.exception.EmailAlreadyExistsException;
import com.uca.juangarcia.ifit.exception.EmailNotFoundException;
import com.uca.juangarcia.ifit.exception.ErrorResponse;
import com.uca.juangarcia.ifit.exception.ExperienceLevelNotFoundException;
import com.uca.juangarcia.ifit.exception.UserIdNotFoundException;
import com.uca.juangarcia.ifit.modules.user.dto.AppUserResponseDto;
import com.uca.juangarcia.ifit.modules.user.dto.CreateAppUserRequestDto;
import com.uca.juangarcia.ifit.modules.user.dto.UpdateAppUserRequestDto;
import com.uca.juangarcia.ifit.modules.user.service.AppUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de usuarios de la aplicación.
 * 
 * Este controlador expone endpoints para realizar operaciones CRUD sobre usuarios,
 * así como funcionalidades específicas del proceso de registro y configuración.
 * 
 * Todos los endpoints están bajo la ruta base {@code /api/v1/users} siguiendo
 * las convenciones RESTful y versionado de API.
 * 
 * 
 * @author Juan Garcia
 * @version 2.0
 * @since 1.0
 */
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "API para gestión de usuarios")
public class AppUserController {

    private final AppUserService userService;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param userService servicio de usuarios
     */
    public AppUserController(AppUserService userService) {
        this.userService = userService;
    }

    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * Endpoint: {@code GET /api/v1/users}
     * 
     * Para grandes conjuntos de datos, se recomienda usar
     * el endpoint paginado {@code GET /api/v1/users/paginated}.
     * 
     * @return ResponseEntity con la lista de usuarios y código 200 OK
     */
    @GetMapping
    @Operation(
        summary = "Obtener todos los usuarios",
        description = "Retorna una lista completa de todos los usuarios registrados en el sistema. " +
                     "Para grandes volúmenes de datos, se recomienda usar el endpoint paginado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppUserResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<List<AppUserResponseDto>> getAllUsers() {
        List<AppUserResponseDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Obtiene usuarios con paginación.
     * 
     * Endpoint: {@code GET /api/v1/users/paginated}
     * 
     * Ejemplo de uso:</strong>
     * GET /api/v1/users/paginated?page=0&size=20&sort=name,asc
     * 
     * @param page número de página (comienza en 0)
     * @param size tamaño de página (elementos por página)
     * @param sortBy campo por el cual ordenar (por defecto: createdAt)
     * @param sortDir dirección de ordenamiento: asc o desc (por defecto: desc)
     * @return ResponseEntity con la página de usuarios y código 200 OK
     */
    @GetMapping("/paginated")
    @Operation(
        summary = "Obtener usuarios paginados",
        description = "Retorna una página de usuarios con soporte para ordenamiento. " +
                     "Útil para grandes conjuntos de datos."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Página de usuarios obtenida exitosamente"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<Page<AppUserResponseDto>> getUsersPaginated(
            @Parameter(description = "Número de página (empieza en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Tamaño de página", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Campo de ordenamiento", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Dirección de ordenamiento (asc/desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<AppUserResponseDto> usersPage = userService.findAllUsers(pageable);
        return ResponseEntity.ok(usersPage);
    }

    /**
     * Obtiene un usuario por su ID.
     * 
     * Endpoint: {@code GET /api/v1/users/{id}}
     * 
     * @param id identificador único del usuario
     * @return ResponseEntity con el usuario y código 200 OK
     * @throws UserIdNotFoundException si no existe el usuario
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Retorna los datos de un usuario específico identificado por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppUserResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<AppUserResponseDto> getUserById(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long id
    ) throws UserIdNotFoundException {
        AppUserResponseDto user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Obtiene un usuario por su email.
     * 
     * Endpoint: {@code GET /api/v1/users/email/{email}}
     * 
     * @param email dirección de email del usuario
     * @return ResponseEntity con el usuario y código 200 OK
     * @throws EmailNotFoundException si no existe el usuario
     */
    @GetMapping("/email/{email}")
    @Operation(
        summary = "Obtener usuario por email",
        description = "Retorna los datos de un usuario específico identificado por su email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado con ese email"
        )
    })
    public ResponseEntity<AppUserResponseDto> getUserByEmail(
            @Parameter(description = "Email del usuario", required = true, example = "juan@example.com")
            @PathVariable String email
    ) throws EmailNotFoundException {
        AppUserResponseDto user = userService.findUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    /**
     * Crea un nuevo usuario.
     * 
     * Endpoint: {@code POST /api/v1/users}
     * 
     * Cuerpo de la petición (JSON):
     * {
     *   "name": "Juan García",
     *   "email": "juan@example.com",
     *   "password": "SecurePass123!"
     * }
     * 
     * @param createDto datos del nuevo usuario (validados)
     * @return ResponseEntity con el usuario creado y código 201 CREATED
     * @throws EmailAlreadyExistsException si el email ya está registrado
     */
    @PostMapping
    @Operation(
        summary = "Crear nuevo usuario",
        description = "Crea un nuevo usuario en el sistema con los datos proporcionados. " +
                     "El email debe ser único y la contraseña será encriptada automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppUserResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "El email ya está registrado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<AppUserResponseDto> createUser(
            @Parameter(description = "Datos del nuevo usuario", required = true)
            @Valid @RequestBody CreateAppUserRequestDto createDto
    ) throws EmailAlreadyExistsException {
        AppUserResponseDto createdUser = userService.createUser(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Actualiza un usuario existente.
     * 
     * Endpoint: {@code PUT /api/v1/users/{id}}
     * 
     * Permite actualizaciones parciales. Solo los campos proporcionados
     * en el DTO serán actualizados.
     * 
     * @param id identificador del usuario a actualizar
     * @param updateDto datos a actualizar (opcionales)
     * @return ResponseEntity con el usuario actualizado y código 200 OK
     * @throws UserIdNotFoundException si no existe el usuario
     * @throws EmailAlreadyExistsException si el nuevo email ya está en uso
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar usuario",
        description = "Actualiza los datos de un usuario existente. Soporta actualizaciones parciales."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Usuario actualizado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "El nuevo email ya está en uso por otro usuario"
        )
    })
    public ResponseEntity<AppUserResponseDto> updateUser(
            @Parameter(description = "ID del usuario a actualizar", required = true, example = "1")
            @PathVariable Long id,
            
            @Parameter(description = "Datos a actualizar", required = true)
            @Valid @RequestBody UpdateAppUserRequestDto updateDto
    ) throws UserIdNotFoundException, EmailAlreadyExistsException {
        AppUserResponseDto updatedUser = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Elimina un usuario del sistema.
     * 
     * Endpoint: {@code DELETE /api/v1/users/{id}}
     * 
     * Advertencia: Esta operación es irreversible.
     * 
     * @param id identificador del usuario a eliminar
     * @return ResponseEntity con código 204 NO CONTENT
     * @throws UserIdNotFoundException si no existe el usuario
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina permanentemente un usuario del sistema. Esta operación es irreversible."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Usuario eliminado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", required = true, example = "1")
            @PathVariable Long id
    ) throws UserIdNotFoundException {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Asigna un tipo de coach a un usuario.
     * 
     * Endpoint: {@code PATCH /api/v1/users/{userId}/assign-coach/{coachId}}
     * 
     * @param userId identificador del usuario
     * @param coachId identificador del tipo de coach
     * @return ResponseEntity con el usuario actualizado y código 200 OK
     * @throws UserIdNotFoundException si no existe el usuario
     * @throws CoachModelTypeNotFoundException si no existe el tipo de coach
     */
    @PatchMapping("/{userId}/assign-coach/{coachId}")
    @Operation(
        summary = "Asignar tipo de coach",
        description = "Asigna un modelo de coach de IA al usuario para personalizar su experiencia"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Coach asignado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario o tipo de coach no encontrado"
        )
    })
    public ResponseEntity<AppUserResponseDto> setCoachModelType(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long userId,
            
            @Parameter(description = "ID del tipo de coach", required = true, example = "1")
            @PathVariable Long coachId
    ) throws UserIdNotFoundException, CoachModelTypeNotFoundException {
        AppUserResponseDto updatedUser = userService.setCoachModelType(userId, coachId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Asigna un nivel de experiencia a un usuario.
     * 
     * Endpoint: {@code PATCH /api/v1/users/{userId}/assign-experience/{levelId}}
     * 
     * @param userId identificador del usuario
     * @param levelId identificador del nivel de experiencia
     * @return ResponseEntity con el usuario actualizado y código 200 OK
     * @throws UserIdNotFoundException si no existe el usuario
     * @throws ExperienceLevelNotFoundException si no existe el nivel de experiencia
     */
    @PatchMapping("/{userId}/assign-experience/{levelId}")
    @Operation(
        summary = "Asignar nivel de experiencia",
        description = "Asigna un nivel de experiencia al usuario para personalizar entrenamientos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Nivel de experiencia asignado exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario o nivel de experiencia no encontrado"
        )
    })
    public ResponseEntity<AppUserResponseDto> setExperienceLevel(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long userId,
            
            @Parameter(description = "ID del nivel de experiencia", required = true, example = "2")
            @PathVariable Long levelId
    ) throws UserIdNotFoundException, ExperienceLevelNotFoundException {
        AppUserResponseDto updatedUser = userService.setExperienceLevel(userId, levelId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Marca el registro de un usuario como completado.
     * 
     * Endpoint: {@code PATCH /api/v1/users/{userId}/complete-registration}
     * 
     * @param userId identificador del usuario
     * @return ResponseEntity con el usuario actualizado y código 200 OK
     * @throws UserIdNotFoundException si no existe el usuario
     */
    @PatchMapping("/{userId}/complete-registration")
    @Operation(
        summary = "Completar registro de usuario",
        description = "Marca el proceso de registro/onboarding del usuario como completado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Registro marcado como completo exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado"
        )
    })
    public ResponseEntity<AppUserResponseDto> markRegistrationComplete(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long userId
    ) throws UserIdNotFoundException {
        AppUserResponseDto updatedUser = userService.markRegistrationComplete(userId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Verifica si existe un usuario con el email especificado.
     * 
     * Endpoint: {@code GET /api/v1/users/exists/email/{email}}
     * 
     * @param email email a verificar
     * @return ResponseEntity con true/false y código 200 OK
     */
    @GetMapping("/exists/email/{email}")
    @Operation(
        summary = "Verificar si existe email",
        description = "Verifica si ya existe un usuario registrado con el email especificado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Verificación completada"
        )
    })
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Email a verificar", required = true, example = "juan@example.com")
            @PathVariable String email
    ) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}
