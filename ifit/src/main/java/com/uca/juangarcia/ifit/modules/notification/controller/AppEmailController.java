package com.uca.juangarcia.ifit.modules.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uca.juangarcia.ifit.modules.notification.dto.EmailResponseDto;
import com.uca.juangarcia.ifit.modules.notification.dto.SupportTicketRequestDto;
import com.uca.juangarcia.ifit.modules.notification.service.AppEmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controlador REST para la gestión de notificaciones por email.
 *
 * <p>Expone endpoints para que los usuarios autenticados puedan enviar tickets de soporte
 * al equipo técnico de IFit.</p>
 *
 * <p>Todos los endpoints están bajo la ruta base {@code /appemail}.</p>
 *
 * @author Juan Garcia
 * @version 1.0
 * @since 1.0
 */
@RestController
@RequestMapping("/appemail")
@Tag(name = "Email", description = "API para notificaciones y soporte por email")
public class AppEmailController {

    private final AppEmailService appEmailService;

    /**
     * Constructor con inyección de dependencias.
     *
     * @param appEmailService servicio de envío de emails.
     */
    public AppEmailController(AppEmailService appEmailService) {
        this.appEmailService = appEmailService;
    }

    /**
     * Envía un ticket de soporte técnico al buzón de atención de IFit.
     *
     * <p>El usuario autenticado proporciona asunto, categoría y mensaje. El sistema
     * construye un email HTML y lo envía al correo de soporte configurado en
     * {@code ifit.support.email}.</p>
     *
     * <p>Endpoint: {@code POST /appemail/support-ticket}</p>
     *
     * @param request DTO con los datos del ticket (asunto, categoría, mensaje).
     * @param jwt     token JWT del usuario autenticado, inyectado por Spring Security.
     * @return {@link EmailResponseDto} indicando si el envío fue exitoso.
     */
    @Operation(
        summary = "Enviar ticket de soporte",
        description = "Permite al usuario autenticado enviar un ticket de soporte técnico al equipo de IFit."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket enviado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos del ticket inválidos o incompletos"),
        @ApiResponse(responseCode = "401", description = "Token de autenticación no válido o ausente"),
        @ApiResponse(responseCode = "500", description = "Error interno al enviar el email")
    })
    @PostMapping("/support-ticket")
    public ResponseEntity<EmailResponseDto> submitSupportTicket(
            @Valid @RequestBody SupportTicketRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {

        String userName  = jwt.getClaimAsString("preferred_username");
        String userEmail = jwt.getClaimAsString("email");

        if (userEmail == null || userEmail.isBlank()) {
            userEmail = "no-disponible";
        }

        EmailResponseDto response = appEmailService.sendSupportTicketEmail(userName, userEmail, request);
        return ResponseEntity.ok(response);
    }
}
