package com.uca.juangarcia.ifit.modules.coach.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.uca.juangarcia.ifit.modules.coach.dto.CoachModelTypeResponseDto;
import com.uca.juangarcia.ifit.modules.coach.dto.CreateCoachModelTypeRequestDto;
import com.uca.juangarcia.ifit.modules.coach.dto.UpdateCoachModelTypeRequestDto;
import com.uca.juangarcia.ifit.modules.coach.model.CoachModelType;

/**
 * Mapper para convertir entre entidades CoachModelType y sus DTOs.
 * Centraliza la lógica de conversión para mantener la separación de responsabilidades.
 * 
 * <p>Este mapper se encarga de:
 * <ul>
 *   <li>Convertir entidades a DTOs de respuesta</li>
 *   <li>Convertir DTOs de request a entidades nuevas</li>
 *   <li>Actualizar entidades existentes desde DTOs de actualización</li>
 *   <li>Manejar valores nulos de forma segura</li>
 * </ul>
 * 
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@Component
public class CoachModelTypeMapper {

    /**
     * Convierte una entidad CoachModelType a un DTO de respuesta.
     * 
     * <p>Este método mapea todos los campos de la entidad al DTO de respuesta,
     * incluyendo las fechas de auditoría. Todos los campos se copian tal cual,
     * sin transformaciones adicionales.
     * 
     * @param coachModelType la entidad CoachModelType a convertir
     * @return el DTO de respuesta con los datos del tipo de modelo de coach
     * @throws IllegalArgumentException si el coachModelType es nulo
     * 
     * @see CoachModelTypeResponseDto
     */
    public CoachModelTypeResponseDto toResponseDto(CoachModelType coachModelType) {
        if (coachModelType == null) {
            throw new IllegalArgumentException("CoachModelType cannot be null");
        }

        return new CoachModelTypeResponseDto(
            coachModelType.getId(),
            coachModelType.getName(),
            coachModelType.getDescription(),
            coachModelType.getEmojiCharacter(),
            coachModelType.getEnabled(),
            coachModelType.getCreatedAt(),
            coachModelType.getUpdatedAt()
        );
    }

    /**
     * Convierte un DTO de creación a una entidad CoachModelType.
     * 
     * <p>Este método crea una nueva instancia de CoachModelType con los datos
     * proporcionados en el DTO. El ID es generado automáticamente por JPA, y
     * la fecha de creación se establece al momento actual.
     * 
     * <p><strong>Nota:</strong> Este método NO guarda la entidad en la base de datos.
     * El guardado debe ser realizado por el servicio correspondiente.
     * 
     * @param dto el DTO con los datos del nuevo tipo de modelo de coach
     * @return una nueva entidad CoachModelType lista para persistir
     * @throws IllegalArgumentException si el DTO es nulo
     * 
     * @see CreateCoachModelTypeRequestDto
     */
    public CoachModelType toEntity(CreateCoachModelTypeRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateCoachModelTypeRequestDto cannot be null");
        }

        CoachModelType coachModelType = new CoachModelType();
        coachModelType.setName(dto.name());
        coachModelType.setDescription(dto.description());
        coachModelType.setEmojiCharacter(dto.emojiCharacter());
        coachModelType.setEnabled(dto.enabled());
        coachModelType.setCreatedAt(LocalDateTime.now());
        
        return coachModelType;
    }

    /**
     * Convierte un DTO de respuesta a una entidad CoachModelType.
     * 
     * <p>Este método crea una nueva instancia de CoachModelType con los datos
     * proporcionados en el DTO de respuesta. Se utiliza principalmente para
     * reconstruir entidades a partir de datos transferidos.
     * 
     * @param dto el DTO de respuesta con los datos del tipo de modelo de coach
     * @return una nueva entidad CoachModelType con los datos del DTO
     * @throws IllegalArgumentException si el DTO es nulo
     * 
     * @see CoachModelTypeResponseDto
     */
    public CoachModelType toEntity(CoachModelTypeResponseDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CoachModelTypeResponseDto cannot be null");
        }

        CoachModelType coachModelType = new CoachModelType();
        coachModelType.setId(dto.id());
        coachModelType.setName(dto.name());
        coachModelType.setDescription(dto.description());
        coachModelType.setEmojiCharacter(dto.emojiCharacter());
        coachModelType.setEnabled(dto.enabled());
        coachModelType.setCreatedAt(dto.createdAt());
        coachModelType.setUpdatedAt(dto.updatedAt());
        
        return coachModelType;
    }

    /**
     * Actualiza una entidad CoachModelType existente con los datos de un DTO de actualización.
     * 
     * <p>Este método solo actualiza los campos que no son nulos en el DTO,
     * permitiendo actualizaciones parciales. La fecha de actualización se
     * establece automáticamente al momento actual.
     * 
     * <p>Los campos que pueden ser actualizados son:
     * <ul>
     *   <li>name - Nombre del modelo</li>
     *   <li>description - Descripción del modelo</li>
     *   <li>emojiCharacter - Emoji representativo</li>
     *   <li>enabled - Estado de habilitación</li>
     * </ul>
     * 
     * @param coachModelType la entidad existente a actualizar (no nula)
     * @param dto el DTO con los nuevos datos (campos opcionales)
     * @throws IllegalArgumentException si algún parámetro es nulo
     */
    public void updateEntityFromDto(CoachModelType coachModelType, UpdateCoachModelTypeRequestDto dto) {
        if (coachModelType == null) {
            throw new IllegalArgumentException("CoachModelType cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("UpdateCoachModelTypeRequestDto cannot be null");
        }

        // Actualizar solo los campos que no son nulos
        if (dto.name() != null && !dto.name().isBlank()) {
            coachModelType.setName(dto.name());
        }
        
        if (dto.description() != null) {
            coachModelType.setDescription(dto.description());
        }
        
        if (dto.emojiCharacter() != null) {
            coachModelType.setEmojiCharacter(dto.emojiCharacter());
        }
        
        if (dto.enabled() != null) {
            coachModelType.setEnabled(dto.enabled());
        }
        
        // Siempre actualizar la fecha de modificación
        coachModelType.setUpdatedAt(LocalDateTime.now());
    }
}
