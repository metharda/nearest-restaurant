package com.project.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class QueueClientService {

    private static final String QUEUE_SERVICE_URL = "http://localhost:8082/api/queue";
    private static final String QUEUE_NAME = "path-requests";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WebSocketService webSocketService;
    

    public QueueClientService(WebSocketService webSocketService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.webSocketService = webSocketService;
        
        // Create queue if it doesn't exist (we'll handle this with the first request)
    }

    // This method is kept for backward compatibility but doesn't do anything now
    public void registerRequest(String requestId) {
        // We no longer need to store session IDs as we're broadcasting to a general topic
        System.out.println("Path request registered with ID: " + requestId);
    }
    
    /**
     * Starts polling the queue for a specific request result
     * @param requestId The unique ID of the request to poll for
     */
    public void startPollingQueue(String requestId) {
        Thread pollingThread = new Thread(() -> {
            boolean received = false;
            try {
                while (!received) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Content-Type", "application/json");
                    headers.set("QueueName", QUEUE_NAME);
                    headers.set("RequestId", requestId);
                    
                    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                    
                    ResponseEntity<String> response = restTemplate.exchange(
                        QUEUE_SERVICE_URL + "/status", 
                        HttpMethod.GET, 
                        requestEntity, 
                        String.class
                    );
                    
                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isEmpty()) {
                        received = true;
                        // Send to general path topic
                        webSocketService.sendMessage("/topic/paths", response.getBody());
                    }

                    // Wait before polling again to avoid overwhelming the queue service
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Handle exceptions
                webSocketService.sendMessage("/topic/errors", 
                        Map.of("requestId", requestId, "error", e.getMessage()));
            }
        });
        
        // Set as daemon thread so it doesn't prevent application shutdown
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    /**
     * Enqueues a message to the Queue Service
     * @param message The message to enqueue
     */
    public void enqueueMessage(Object message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("QueueName", QUEUE_NAME);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonMessage, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                QUEUE_SERVICE_URL + "/enqueue", 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("Failed to enqueue message: " + response.getBody());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enqueue message", e);
        }
    }

    /**
     * Dequeues a message from the Queue Service
     * @return The dequeued message as a String, or null if queue is empty
     */
    public String dequeueMessage() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("QueueName", QUEUE_NAME);
            
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                QUEUE_SERVICE_URL + "/dequeue", 
                HttpMethod.GET, 
                requestEntity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                System.err.println("Failed to dequeue message: " + response.getBody());
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to dequeue message", e);
        }
    }
    
    /**
     * Dequeues a message from the Queue Service and deserializes it to the specified type
     * @param <T> The type to deserialize the message to
     * @param classType The class of the type to deserialize to
     * @return The dequeued message deserialized to the specified type, or null if queue is empty
     */
    public <T> T dequeueMessage(Class<T> classType) {
        String jsonMessage = dequeueMessage();
        if (jsonMessage == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(jsonMessage, classType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }
}
