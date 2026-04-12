package com.ifit.ronnie.modules.message.service;

import org.springframework.stereotype.Service;

import com.ifit.ronnie.modules.message.repository.MessageRepository;

@Service
public class MessageService {
    
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Integer getMaxMemoryId() {
        return messageRepository.findByDistinctMemoryIds()
                .stream()
                .map(Integer::parseInt)
                .max(Integer::compareTo)
                .orElse(1);

    }
}