package com.ifit.ronnie.modules.message.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ifit.ronnie.modules.message.controller.dto.MessageResponseDto;
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
                .map(max -> max + 1)
                .orElse(1);
    }

    public List<MessageResponseDto> getMessagesByUserAndCoach(String userId, String coachName) {
        return messageRepository.findByUserIdAndCoachNameOrderByCreatedAtAsc(userId, coachName)
                .stream()
                .map(m -> new MessageResponseDto(
                        m.getId(),
                        m.getMemoryId(),
                        m.getMessageType().getName(),
                        cleanMessage(m.getMessage()),
                        m.getCreatedAt()))
                .filter(dto -> !dto.message().isBlank())
                .toList();
    }

    private String cleanMessage(String message) {
        String[] markers = { "Prompt embebido", "Answer using the following information", "PERFIL DEL USUARIO"};
        int cutIndex = message.length();
        for (String marker : markers) {
            int idx = message.indexOf(marker);
            if (idx != -1 && idx < cutIndex) {
                cutIndex = idx;
            }
        }
        return message.substring(0, cutIndex).stripTrailing();
    }
}
