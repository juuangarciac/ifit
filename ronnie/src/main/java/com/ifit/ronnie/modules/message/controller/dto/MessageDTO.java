package com.ifit.ronnie.modules.message.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO de request/response para chatear con un modelo de AI")
public record MessageDto(
    @NotNull(message = "memoryId cannot be null")
    Integer memoryId,
    @NotBlank(message = "message cannot be null or blank")
    String message
) {}
