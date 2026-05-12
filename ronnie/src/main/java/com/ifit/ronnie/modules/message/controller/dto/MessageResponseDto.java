package com.ifit.ronnie.modules.message.controller.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de respuesta para un mensaje del historial de chat")
public record MessageResponseDto(
    Long id,
    String memoryId,
    String messageType,
    String message,
    LocalDateTime createdAt
) {}
