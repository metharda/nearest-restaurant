package com.project.controller;

import com.project.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class WebSocketTestController {

    private final WebSocketService webSocketService;

    @Autowired
    public WebSocketTestController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @GetMapping("/send-test-message")
    public ResponseEntity<?> sendTestMessage(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "Test message from server") String message) {
        
        // Send it via WebSocket
        Map<String, Object> payload = Map.of(
            "message", message,
            "timestamp", System.currentTimeMillis(),
            "type", "TEST_MESSAGE"
        );
        
        webSocketService.sendMessage("/topic/paths/" + sessionId, payload);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Test message sent to session: " + sessionId
        ));
    }
    
    @GetMapping("/send-test-path")
    public ResponseEntity<?> sendTestPath(@RequestParam String sessionId) {
        // Create a sample path data as Map
        Map<String, Object> pathData = createSamplePathData();
        
        // Send it via WebSocket
        webSocketService.sendMessage("/topic/paths/" + sessionId, pathData);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Test path sent to session: " + sessionId
        ));
    }
    
    private Map<String, Object> createSamplePathData() {
        // Create sample path points (Istanbul route)
        List<Map<String, Double>> pathPoints = new ArrayList<>();
        
        // Add some sample points (Taksim to Sultanahmet)
        pathPoints.add(Map.of("latitude", 41.0370, "longitude", 28.9850)); // Taksim
        pathPoints.add(Map.of("latitude", 41.0352, "longitude", 28.9775));
        pathPoints.add(Map.of("latitude", 41.0314, "longitude", 28.9752));
        pathPoints.add(Map.of("latitude", 41.0272, "longitude", 28.9744));
        pathPoints.add(Map.of("latitude", 41.0223, "longitude", 28.9747));
        pathPoints.add(Map.of("latitude", 41.0159, "longitude", 28.9768));
        pathPoints.add(Map.of("latitude", 41.0082, "longitude", 28.9784)); // Sultanahmet
        
        // Create the response object as a map
        return Map.of(
            "requestId", "test-request-123",
            "restaurantId", "123456",
            "path", pathPoints,
            "status", "COMPLETED",
            "distance", 3500.0, // 3.5 km
            "duration", 2400 // 40 minutes
        );
    }
}
