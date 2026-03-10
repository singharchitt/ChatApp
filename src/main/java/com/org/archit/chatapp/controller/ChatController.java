package com.org.archit.chatapp.controller;

import com.org.archit.chatapp.model.ChatMessage;
import com.org.archit.chatapp.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    @Autowired
    private ChatMessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Track sessionId -> username
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();

    @MessageMapping("/sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {
        // Validate input
        if (message.getSender() == null || message.getSender().isBlank()) {
            message.setSender("Anonymous");
        }
        if (message.getContent() == null || message.getContent().isBlank()) {
            return null; // drop empty messages
        }
        message.setType("CHAT");
        message.setTimestamp(LocalDateTime.now());
        messageRepository.save(message);
        return message;
    }

    @MessageMapping("/join")
    @SendTo("/topic/public")
    public ChatMessage joinUser(ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String sender = (message.getSender() == null || message.getSender().isBlank())
                ? "Anonymous" : message.getSender().trim();
        sessionUsers.put(sessionId, sender);

        ChatMessage joinMsg = new ChatMessage(sender, sender + " joined the chat", "JOIN");
        joinMsg.setTimestamp(LocalDateTime.now());
        messageRepository.save(joinMsg);
        return joinMsg;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String username = sessionUsers.remove(sessionId);
        if (username != null) {
            ChatMessage leaveMsg = new ChatMessage(username, username + " left the chat", "LEAVE");
            leaveMsg.setTimestamp(LocalDateTime.now());
            messageRepository.save(leaveMsg);
            messagingTemplate.convertAndSend("/topic/public", leaveMsg);
        }
    }

    @GetMapping("/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory() {
        return messageRepository.findTop50ByTypeOrderByTimestampAsc("CHAT");
    }
}
