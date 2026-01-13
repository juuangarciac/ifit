package com.ifit.ronnie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifit.ronnie.controller.DTO.MaxMemoryIdDTO;
import com.ifit.ronnie.service.MessageService;

@RestController
@RequestMapping("/messages")
public class MessageController {
    
    @Autowired
    private MessageService messageService;

    @GetMapping("/max-memory-id")
    public ResponseEntity<MaxMemoryIdDTO> getMaxMemoryId() {
     return ResponseEntity.status(HttpStatus.OK).body(new MaxMemoryIdDTO( messageService.getMaxMemoryId() ));    
    }
}
