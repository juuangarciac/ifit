package com.ifit.ronnie.modules.message.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ifit.ronnie.modules.message.repository.MessageRepository;

@Service
public class MessageService {
    

    @Autowired
    private MessageRepository messageRepository;

    public Integer getMaxMemoryId() {
        return messageRepository.findByDistinctMemoryIds()
                .stream()
                .map(Integer::parseInt)
                .max(Integer::compareTo)
                .orElse(1);

    }
}