package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import java.time.LocalDateTime;

public record AnswerDto(
    Long answerId,
    String questionText,
    String selectedOption,
    String additionalText,
    String aiDescription,
    LocalDateTime answeredAt
) {}
