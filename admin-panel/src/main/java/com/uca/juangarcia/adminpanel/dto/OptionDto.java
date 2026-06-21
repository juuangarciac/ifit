package com.uca.juangarcia.adminpanel.dto;

public record OptionDto(
    Long id,
    String text,
    Boolean requiresTextInput,
    String textInputPrompt,
    String textInputPlaceholder
) {}
