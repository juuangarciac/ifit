package com.uca.juangarcia.ifit.modules.notification.dto;

import jakarta.validation.constraints.NotBlank;

public class SupportTicketRequestDto {

    @NotBlank(message = "El asunto no puede estar vacío")
    private String subject;

    @NotBlank(message = "La categoría no puede estar vacía")
    private String category;

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;

    public SupportTicketRequestDto() {}

    public SupportTicketRequestDto(String subject, String category, String message) {
        this.subject = subject;
        this.category = category;
        this.message = message;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
