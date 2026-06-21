package com.uca.juangarcia.adminpanel.dto;

import java.time.LocalDateTime;

public record QuestionnaireWithFirstQuestionDto(
    Long id,
    String name,
    String description,
    String coachModelTypeName,
    String coachModelTypeEmoji,
    String experienceLevelName,
    Boolean isEnabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    QuestionDto firstQuestion
) {}
