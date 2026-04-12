package com.uca.juangarcia.ifit.modules.questionnaire.dto;

import java.util.List;

import com.uca.juangarcia.ifit.modules.questionnaire.model.QuestionType;

public record QuestionDto(
    Long id,
    String text,
    QuestionType type,
    List<OptionDto> options
) {}
