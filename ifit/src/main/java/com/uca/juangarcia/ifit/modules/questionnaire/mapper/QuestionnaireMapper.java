package com.uca.juangarcia.ifit.modules.questionnaire.mapper;

import org.springframework.stereotype.Component;

import com.uca.juangarcia.ifit.modules.questionnaire.dto.CreateQuestionnaireRequestDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireDTO;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireSummaryDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.QuestionnaireWithFirstQuestionDto;
import com.uca.juangarcia.ifit.modules.questionnaire.dto.UpdateQuestionnaireRequestDto;
import com.uca.juangarcia.ifit.modules.questionnaire.model.Questionnaire;

/**
 * Mapper para convertir entre entidades Questionnaire y sus DTOs.
 * 
 * @author Juan Garcia
 * @version 2.0
 * @since 1.0
 */
@Component
public class QuestionnaireMapper {

    /**
     * Convierte una entidad Questionnaire a un DTO completo.
     * 
     * @param questionnaire la entidad Questionnaire a convertir
     * @return el DTO con todos los datos del cuestionario
     * @throws IllegalArgumentException si el cuestionario es nulo
     */
    public QuestionnaireDTO toDto(Questionnaire questionnaire) {
        if (questionnaire == null) {
            throw new IllegalArgumentException("Questionnaire cannot be null");
        }

        return new QuestionnaireDTO(
            questionnaire.getId(),
            questionnaire.getName(),
            questionnaire.getDescription(),
            questionnaire.getCoachModelType() != null ? questionnaire.getCoachModelType().getName() : null,
            questionnaire.getCoachModelType() != null ? questionnaire.getCoachModelType().getEmojiCharacter() : null,
            questionnaire.getExperienceLevel() != null ? questionnaire.getExperienceLevel().getName() : null,
            questionnaire.getFirstQuestion() != null ? questionnaire.getFirstQuestion().getId() : null,
            questionnaire.getIsEnabled(),
            questionnaire.getCreatedAt(),
            questionnaire.getUpdatedAt()
        );
    }

    /**
     * Convierte una entidad Questionnaire a un DTO de resumen compacto.
     * 
     * @param questionnaire la entidad Questionnaire a convertir
     * @return el DTO de resumen con información básica
     * @throws IllegalArgumentException si el cuestionario es nulo
     */
    public QuestionnaireSummaryDto toSummaryDto(Questionnaire questionnaire) {
        if (questionnaire == null) {
            throw new IllegalArgumentException("Questionnaire cannot be null");
        }

        return new QuestionnaireSummaryDto(
            questionnaire.getId(),
            questionnaire.getName(),
            questionnaire.getDescription(),
            questionnaire.getCoachModelType() != null ? questionnaire.getCoachModelType().getName() : null,
            questionnaire.getCoachModelType() != null ? questionnaire.getCoachModelType().getEmojiCharacter() : null,
            questionnaire.getExperienceLevel() != null ? questionnaire.getExperienceLevel().getName() : null,
            questionnaire.getIsEnabled()
        );
    }

    /**
     * Convierte una entidad Questionnaire a un DTO con la primera pregunta incluida.
     * 
     * @param questionnaire la entidad Questionnaire
     * @param firstQuestionDto el DTO de la primera pregunta con sus opciones
     * @return el DTO con cuestionario y primera pregunta
     * @throws IllegalArgumentException si algún parámetro es nulo
     */
    public QuestionnaireWithFirstQuestionDto toWithFirstQuestionDto(
            Questionnaire questionnaire, 
            QuestionDTO firstQuestionDto) {
        
        if (questionnaire == null) {
            throw new IllegalArgumentException("Questionnaire cannot be null");
        }
        if (firstQuestionDto == null) {
            throw new IllegalArgumentException("First question DTO cannot be null");
        }

        return new QuestionnaireWithFirstQuestionDto(
            questionnaire.getId(),
            questionnaire.getName(),
            questionnaire.getDescription(),
            questionnaire.getCoachModelType() != null ? questionnaire.getCoachModelType().getName() : null,
            questionnaire.getCoachModelType() != null ? questionnaire.getCoachModelType().getEmojiCharacter() : null,
            questionnaire.getExperienceLevel() != null ? questionnaire.getExperienceLevel().getName() : null,
            questionnaire.getIsEnabled(),
            questionnaire.getCreatedAt(),
            questionnaire.getUpdatedAt(),
            firstQuestionDto
        );
    }

    /**
     * Convierte un DTO de creación a una nueva entidad Questionnaire.
     * 
     * @param dto el DTO con los datos del nuevo cuestionario
     * @return una nueva entidad Questionnaire con los campos básicos asignados
     * @throws IllegalArgumentException si el DTO es nulo
     */
    public Questionnaire toEntity(CreateQuestionnaireRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("CreateQuestionnaireRequestDto cannot be null");
        }

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setName(dto.name());
        questionnaire.setDescription(dto.description());
        // Las relaciones se asignan en el servicio
        // createdAt y updatedAt se establecen automáticamente por @PrePersist
        
        return questionnaire;
    }

    /**
     * Actualiza una entidad Questionnaire existente con los datos de un DTO de actualización.
     * 
     * @param questionnaire la entidad existente a actualizar
     * @param dto el DTO con los nuevos datos (campos opcionales)
     * @throws IllegalArgumentException si algún parámetro es nulo
     */
    public void updateEntityFromDto(Questionnaire questionnaire, UpdateQuestionnaireRequestDto dto) {
        if (questionnaire == null) {
            throw new IllegalArgumentException("Questionnaire cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("UpdateQuestionnaireRequestDto cannot be null");
        }

        if (dto.name() != null && !dto.name().isBlank()) {
            questionnaire.setName(dto.name());
        }
        
        if (dto.description() != null && !dto.description().isBlank()) {
            questionnaire.setDescription(dto.description());
        }
        
        if (dto.isEnabled() != null) {
            questionnaire.setIsEnabled(dto.isEnabled());
        }
        
        // Las relaciones se manejan en el servicio
        // updatedAt se actualiza automáticamente por @PreUpdate
    }
}