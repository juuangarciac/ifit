package com.ifit.ronnie.configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ifit.ronnie.modules.message.mapper.ChatMessageMapper;
import com.ifit.ronnie.modules.message.model.Message;
import com.ifit.ronnie.modules.message.repository.MessageRepository;
import com.ifit.ronnie.modules.message.repository.MessageTypeRepository;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

@Component
class PersistentChatMemoryStore implements ChatMemoryStore {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    MessageTypeRepository messageTypeRepository;

    @Autowired
    ChatMessageMapper chatMessageMapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        try{
            System.out.println("PersistentChatMemory.getMessages");
            
            List<Message> messages = messageRepository.findByMemoryId((String) memoryId);
            if(messages == null || messages.isEmpty()) return new ArrayList<>();

            List<ChatMessage> chatMessages = new ArrayList<>();

            for(Message message: messages) {
                if(message.getMessageType().getName().equals("user")){
                    UserMessage userMessage = new UserMessage(message.getMessage());
                    chatMessages.add(userMessage);
    
                }else if(message.getMessageType().getName().equals("ai")){
                    AiMessage aiMessage = new AiMessage(message.getMessage());
                    chatMessages.add(aiMessage);
    
                }else if(message.getMessageType().getName().equals("system")){
                    SystemMessage systemMessage = new SystemMessage(message.getMessage());;
                    chatMessages.add(systemMessage);
                    
                }else{
                    throw new IllegalArgumentException("Message type cannot be found.");
                }
            }
    
            return chatMessages;
        }catch(IllegalArgumentException e){
            System.out.println("Error: " + e.getMessage() + " at PersistentChatMemoryStore.getMessages(Object memoryId).");
            return new ArrayList<>();
        }    
    }
    
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        try {
            System.out.println("PersistentChatMemory.updateMessages");
            ChatMessage newMessage = messages.getLast();
            messageRepository.save(chatMessageMapper.toChat((String)memoryId, newMessage, LocalDateTime.now()));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " at PersistentChatMemoryStore.updateMessages(Object memoryId, List<ChatMessage> messages).");
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
      messageRepository.deleteByMemoryId((String) memoryId);
    }
}

