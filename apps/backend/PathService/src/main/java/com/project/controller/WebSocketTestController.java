package com.project.controller;

import com.project.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.datastructures.HashMap;
import com.project.datastructures.Linkedlist;

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
            @RequestParam String sessionId,            @RequestParam(defaultValue = "Test message from server") String message) {
        
        // Send it via WebSocket
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("message", message);
        payload.put("timestamp", System.currentTimeMillis());
        payload.put("type", "TEST_MESSAGE");
        
        webSocketService.sendMessage("/topic/paths/" + sessionId, payload);
        
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("message", "Test message sent to session: " + sessionId);
        
        return ResponseEntity.ok(responseMap);
    }
    
    @GetMapping("/send-test-path")    public ResponseEntity<?> sendTestPath(@RequestParam String sessionId) {
        // Create a sample path data as HashMap
        HashMap<String, Object> pathData = createSamplePathData();
        
        // Send it via WebSocket
        webSocketService.sendMessage("/topic/paths/" + sessionId, pathData);
        
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        responseMap.put("message", "Test path sent to session: " + sessionId);
        
        return ResponseEntity.ok(responseMap);
    }      private HashMap<String, Object> createSamplePathData() {
        // Create sample path points (Istanbul route)
        Linkedlist<HashMap<String, Double>> pathPoints = new Linkedlist<>();
        
        // Add some sample points (Taksim to Sultanahmet)
        HashMap<String, Double> point1 = new HashMap<>();
        point1.put("latitude", 41.0370);
        point1.put("longitude", 28.9850);
        pathPoints.add(point1); // Taksim
        
        HashMap<String, Double> point2 = new HashMap<>();
        point2.put("latitude", 41.0352);
        point2.put("longitude", 28.9775);
        pathPoints.add(point2);
        
        HashMap<String, Double> point3 = new HashMap<>();
        point3.put("latitude", 41.0314);
        point3.put("longitude", 28.9752);
        pathPoints.add(point3);
        
        HashMap<String, Double> point4 = new HashMap<>();
        point4.put("latitude", 41.0272);
        point4.put("longitude", 28.9744);
        pathPoints.add(point4);
        
        HashMap<String, Double> point5 = new HashMap<>();
        point5.put("latitude", 41.0223);
        point5.put("longitude", 28.9747);
        pathPoints.add(point5);
        
        HashMap<String, Double> point6 = new HashMap<>();
        point6.put("latitude", 41.0159);
        point6.put("longitude", 28.9768);
        pathPoints.add(point6);
        
        HashMap<String, Double> point7 = new HashMap<>();
        point7.put("latitude", 41.0082);
        point7.put("longitude", 28.9784);
        pathPoints.add(point7); // Sultanahmet
        
        // Create the response object as a map
        HashMap<String, Object> result = new HashMap<>();
        result.put("requestId", "test-request-123");
        result.put("restaurantId", "123456");
        result.put("path", pathPoints);
        result.put("status", "COMPLETED");
        result.put("distance", 3500.0); // 3.5 km
        result.put("duration", 2400); // 40 minutes
        
        return result;
    }
}
