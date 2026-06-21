package com.uca.juangarcia.adminpanel.dto;

import java.util.List;

public record QuestionDto(
    Long id,
    String text,
    String type,
    List<OptionDto> options
) {}
