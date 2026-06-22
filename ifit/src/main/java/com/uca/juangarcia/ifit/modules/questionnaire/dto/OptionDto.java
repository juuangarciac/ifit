package com.uca.juangarcia.ifit.modules.questionnaire.dto;

public record OptionDto(
    Long id,
    String text,
    Boolean requiresTextInput,
    String textInputPrompt,
    String textInputPlaceholder
) {}
