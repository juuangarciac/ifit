package com.ifit.ronnie.modules.message.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.modules.message.controller.dto.MaxMemoryIdDto;
import com.ifit.ronnie.modules.message.service.MessageService;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/max-memory-id")
    public ResponseEntity<MaxMemoryIdDto> getMaxMemoryId() {
        return ResponseEntity.status(HttpStatus.OK).body(new MaxMemoryIdDto(messageService.getMaxMemoryId()));
    }
}
