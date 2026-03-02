package com.ifit.ronnie.modules.message.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO de request/response para chatear con un modelo de AI")
public class MessageDTO {

    /**
     * Id de memoria con el que se identifica una conversación en la base de datos
     */
    @NotNull(message = "El memoryId no puede ser null")
    private Integer memoryId;

    /**
     * Mensaje que se le envía al coach.
     */
    @NotBlank(message = "Mensaje enviado al modelo no puede ser null o vacío.")
    private String message;

    // Constructor vacío (necesario para Jackson)
    public MessageDTO() {
    }

    // Constructor con parámetros
    public MessageDTO(Integer memoryId, String message) {
        this.memoryId = memoryId;
        this.message = message;
    }

    // Getters y Setters
    public Integer getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(Integer memoryId) {
        this.memoryId = memoryId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageDTO{" +
                "memoryId=" + memoryId +
                ", message='" + message + '\'' +
                '}';
    }
}