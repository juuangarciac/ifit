package com.ifit.ronnie.modules.message.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.ifit.ronnie.modules.message.model.Message;
import com.ifit.ronnie.modules.message.model.MessageType;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

@Component
public class ChatMessageMapper {

    private MessageType findTypeByName(List<MessageType> types, String targetName) {
        return types.stream()
            .filter(type -> type.getName().equalsIgnoreCase(targetName))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Message type not found: " + targetName));
    }

    public Message toChat(String memoryId, ChatMessage message, LocalDateTime createdAt, List<MessageType> allMessageTypes) {
        try {
            Message chat = new Message();
            chat.setMemoryId((String)memoryId);
            
            String chatMessage = "";
            MessageType chatMessageType = new MessageType();
            if(message instanceof UserMessage){
                chatMessage = ((UserMessage) message).hasSingleText() ? ((UserMessage) message).singleText() : "";
                chatMessageType = findTypeByName(allMessageTypes, "user");
            
            }else if(message instanceof AiMessage){
                chatMessage = ((AiMessage) message).text();
                chatMessageType = findTypeByName(allMessageTypes, "ai");
    
            }else if(message instanceof SystemMessage){
                chatMessage = ((SystemMessage) message).text();
                chatMessageType = findTypeByName(allMessageTypes, "system");
                
            }

            chat.setMessage(chatMessage);
            chat.setMessageType(chatMessageType);
            chat.setCreatedAt(createdAt);

            return chat;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map ChatMessage to Chat: " + e.getMessage());
        }
    }
}

