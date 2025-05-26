package com.project.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    // Constructor with SimpMessagingTemplate
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        System.out.println("WebSocketService initialized with proper implementation");
    }
      
    public void sendMessage(String destination, Object payload) {
        System.out.println("WebSocket: Sending message to destination: " + destination);
        System.out.println("WebSocket: Payload: " + payload);
        
        // Send message to the specified destination
        messagingTemplate.convertAndSend(destination, payload);
    }
}
