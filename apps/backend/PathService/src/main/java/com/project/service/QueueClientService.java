package com.project.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QueueClientService {

    private static final String QUEUE_SERVICE_URL = "http://localhost:8082/api/queue";
    private static final String QUEUE_NAME = "path-requests";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public QueueClientService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        
        // Create queue if it doesn't exist (we'll handle this with the first request)
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
