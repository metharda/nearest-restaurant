package com.project.service;

import com.project.dto.response.PathResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueueListenerService {

    private final WebSocketService webSocketService;
    private final RestTemplate restTemplate;
    private final Map<String, String> requestIdToSessionId = new ConcurrentHashMap<>();

    public QueueListenerService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
        this.restTemplate = new RestTemplate();
    }

    public void registerRequest(String requestId, String sessionId) {
        requestIdToSessionId.put(requestId, sessionId);
    }

    public void startPollingQueue(String requestId, String queueEndpoint) {
        Thread pollingThread = new Thread(() -> {
            boolean received = false;
            try {
                while (!received) {
                    ResponseEntity<PathResponseDto> response = restTemplate.getForEntity(
                            queueEndpoint + "/" + requestId,
                            PathResponseDto.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        received = true;
                        String sessionId = requestIdToSessionId.get(requestId);
                        if (sessionId != null) {
                            // Send to specific user topic
                            webSocketService.sendMessage("/topic/paths/" + sessionId, response.getBody());
                            // Clean up
                            requestIdToSessionId.remove(requestId);
                        }
                    }

                    // Wait before polling again to avoid overwhelming the queue service
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Handle exceptions, may want to inform client about errors
                webSocketService.sendMessage("/topic/errors", 
                        Map.of("requestId", requestId, "error", e.getMessage()));
            }
        });
        
        // Set as daemon thread so it doesn't prevent application shutdown
        pollingThread.setDaemon(true);
        pollingThread.start();
    }
}
