package com.ifit.ronnie.modules.message.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifit.ronnie.configuration.JwtUtils;
import com.ifit.ronnie.modules.message.controller.dto.MaxMemoryIdDto;
import com.ifit.ronnie.modules.message.controller.dto.MessageResponseDto;
import com.ifit.ronnie.modules.message.service.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public MessageController(MessageService messageService, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/max-memory-id")
    public ResponseEntity<MaxMemoryIdDto> getMaxMemoryId() {
        return ResponseEntity.status(HttpStatus.OK).body(new MaxMemoryIdDto(messageService.getMaxMemoryId()));
    }

    @Operation(summary = "Historial de chat de un usuario con un coach",
            description = "Devuelve todos los mensajes de un usuario con un coach concreto, ordenados cronológicamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "No se encontraron mensajes para ese usuario y coach")
    })
    @GetMapping("/user/{userId}/coach/{coachName}")
    public ResponseEntity<List<MessageResponseDto>> getMessagesByUserAndCoach(
            @PathVariable String userId,
            @PathVariable String coachName,
        HttpServletRequest request) {
        
        String keycloakUserId = JwtUtils.extractUserId(request, objectMapper);
        List<MessageResponseDto> messages = messageService.getMessagesByUserAndCoach(keycloakUserId, coachName);
        if (messages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(messages);
    }
}
