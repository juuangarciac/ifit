package com.uca.juangarcia.ifit.modules.user.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.uca.juangarcia.ifit.modules.user.dto.AppUserResponseDto;
import com.uca.juangarcia.ifit.modules.user.dto.CreateAppUserRequestDto;
import com.uca.juangarcia.ifit.modules.user.model.AppUser;

/**
 * Mapper para convertir entre entidades AppUser y sus DTOs.
 * Centraliza la lógica de conversión para mantener la separación de responsabilidades.
 * 
 * <p>Este mapper se encarga de:
 * <ul>
 *   <li>Convertir entidades a DTOs de respuesta</li>
 *   <li>Convertir DTOs de request a entidades</li>
 *   <li>Manejar valores nulos de forma segura</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Component
public class AppUserMapper {

    /**
     * Convierte una entidad AppUser a un DTO de respuesta.
     * 
     * <p>Este método mapea todos los campos de la entidad, incluyendo las relaciones
     * con otras entidades (rol, coach, nivel de experiencia). Si alguna relación es
     * nula, se asigna null al campo correspondiente en el DTO.
     * 
     * @param user la entidad AppUser a convertir
     * @return el DTO de respuesta con los datos del usuario
     * @throws IllegalArgumentException si el usuario es nulo
     * 
     * @see AppUserResponseDto
     */
    public AppUserResponseDto toResponseDto(AppUser user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        return new AppUserResponseDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.isRegistrationComplete(),
            user.isVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getRole() != null ? user.getRole().getName() : null,
            user.getCoachModelType() != null ? user.getCoachModelType().getName() : null,
            user.getExperienceLevel() != null ? user.getExperienceLevel().getName() : null,
            user.getKeycloakId(),
            user.getVerificationCode()
        );
    }
    
    /**
     * Convierte un DTO de usuario a una entidad AppUser.
     * 
     * <p>Este método mapea todos los campos del DTO a la entidad, incluyendo
     * valores nulos. Si algún campo del DTO es nulo, se asigna null al campo
     * correspondiente en la entidad.
     * 
     * @param userDto el DTO con los datos del usuario
     * @return la entidad AppUser con los datos mapeados
     * @throws IllegalArgumentException si el DTO es nulo
     */
    public AppUser toEntity(AppUserResponseDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("AppUserDto cannot be null");
        }

        AppUser user = new AppUser();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setIsRegistrationComplete(userDto.isRegistrationComplete());
        user.setVerified(userDto.isVerified());
        user.setCreatedAt(userDto.getCreatedAt());
        user.setUpdatedAt(userDto.getUpdatedAt());
        
        
        return user;
    }

    /**
     * Convierte un DTO de creación de usuario a una entidad AppUser.
     * 
     * <p>Este método crea una nueva instancia de AppUser con los datos básicos
     * proporcionados en el DTO. Los campos como createdAt, rol, etc. deben ser
     * asignados posteriormente por el servicio.
     * 
     * <p><strong>Nota:</strong> La contraseña debe ser encriptada antes de guardar
     * la entidad. Este mapper NO encripta la contraseña.
     * 
     * @param dto el DTO con los datos del nuevo usuario
     * @return una nueva entidad AppUser con los campos básicos asignados
     * @throws IllegalArgumentException si el DTO es nulo
     * 
     * @see CreateAppUserRequestDto
     */
    public AppUser toEntity(CreateAppUserRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateAppUserRequestDto cannot be null");
        }

        AppUser user = new AppUser();
        user.setName(dto.name());
        user.setPassword(dto.password()); // NOTA: Debe ser encriptada por el servicio
        user.setEmail(dto.email());
        user.setKeycloakId(dto.keycloakId());
        user.setCreatedAt(LocalDateTime.now());
        user.setVerified(false);
        user.setIsRegistrationComplete(false);
        
        return user;
    }

    /**
     * Actualiza una entidad AppUser existente con los datos de un DTO de actualización.
     * 
     * <p>Este método solo actualiza los campos que no son nulos en el DTO,
     * permitiendo actualizaciones parciales. Además, actualiza automáticamente
     * el campo updatedAt con la fecha y hora actual.
     * 
     * @param user la entidad existente a actualizar
     * @param dto el DTO con los nuevos datos (campos opcionales)
     * @throws IllegalArgumentException si algún parámetro es nulo
     */
    public void updateEntityFromDto(AppUser user, com.uca.juangarcia.ifit.modules.user.dto.UpdateAppUserRequestDto dto) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("UpdateAppUserRequestDto cannot be null");
        }

        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }
        
        if (dto.email() != null && !dto.email().isBlank()) {
            user.setEmail(dto.email());
        }
        
        user.setUpdatedAt(LocalDateTime.now());
    }
}
