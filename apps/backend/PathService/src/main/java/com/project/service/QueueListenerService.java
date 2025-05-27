package com.project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.datastructures.Graph;
import com.project.datastructures.HashMap;
import com.project.datastructures.Linkedlist;
import com.project.datastructures.Node;
import com.project.entity.Location;
import com.project.utils.AStar;

@Service
public class QueueListenerService {

    private static final String QUEUE_SERVICE_URL = "http://localhost:8082/api/queue";
    private static final String QUEUE_NAME = "path-requests";
    private static final String WEBSOCKET_TOPIC = "/topic/paths";
    private static final String ERROR_TOPIC = "/topic/errors";
      private final WebSocketService webSocketService;
    private final RestTemplate restTemplate;
    private Thread pollingThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public QueueListenerService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Start the continuous queue polling when the service starts
     */
    @PostConstruct
    public void init() {
        startQueuePolling();
    }

    /**
     * Start a background thread that continuously polls the queue
     * and broadcasts messages to all connected clients
     */
    public void startQueuePolling() {
        if (running.compareAndSet(false, true)) {
            pollingThread = new Thread(() -> {
                try {
                    while (running.get() && !Thread.currentThread().isInterrupted()) {
                        try {
                            // Dequeue a message
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
                            System.out.println("Polling queue: " + QUEUE_NAME + ", Response: " + response.getBody());
                            
                            // Only process 200 OK responses that contain data
                            if (response.getStatusCode().is2xxSuccessful() && response.getStatusCode() != HttpStatus.NO_CONTENT) {
                                String responseBody = response.getBody();
                                // Check for valid JSON data
                                if (responseBody != null && !responseBody.equals("null") && !responseBody.isEmpty() && !responseBody.trim().equals("{}")) {                                    Graph graph = Graph.getInstance();
                                    JsonNode body = objectMapper.readTree(responseBody);
                                    Long restaurantId = body.get("restaurantId").asLong();
                                    Location restaurant = graph.findLocationById(graph.getRestaurants(), restaurantId);
                                    
                                    // Restaurant ve user_location null kontrolü
                                    if (restaurant == null || graph.getUser_location() == null) {
                                        // Restaurant veya kullanıcı konumu bulunamadığında hata mesajı gönder
                                        HashMap<String, Object> errorJson = new HashMap<>();
                                        errorJson.put("error", restaurant == null ? 
                                            "Restaurant with ID " + restaurantId + " not found" : 
                                            "User location not set");
                                        errorJson.put("restaurantId", restaurantId.toString());
                                        errorJson.put("status", "ERROR");
                                        
                                        if (body.has("requestId")) {
                                            errorJson.put("requestId", body.get("requestId").asText());
                                        }
                                        
                                        String errorResponse = objectMapper.writeValueAsString(errorJson);
                                        webSocketService.sendMessage(ERROR_TOPIC, errorResponse);
                                        System.err.println("Cannot calculate path: " + 
                                            (restaurant == null ? "Restaurant not found" : "User location not set"));
                                        continue;
                                    }
                                    
                                    // A* algoritması ile yol hesapla
                                    Linkedlist<Location> path = AStar.findPath(graph, graph.getUser_location(), restaurant);
                                    
                                    if (path != null) {
                                        Location[] path_locations = new Location[path.length()];
                                        Node<Location> current = path.head;

                                        for(int i = 0; i<path.length(); ++i) {
                                            path_locations[i] = current.value;
                                            current = current.next;
                                        }
                                        
                                        // Path verilerini içeren JSON oluştur
                                        HashMap<String, Object> responseJson = new HashMap<>();
                                        responseJson.put("requestId", body.has("requestId") ? body.get("requestId").asText() : "unknown");
                                        responseJson.put("restaurantId", restaurantId.toString());
                                        responseJson.put("path", path_locations);
                                        responseJson.put("status", "COMPLETED");
                                        
                                        // Eğer latitude ve longitude bilgileri varsa ekle
                                        if (body.has("latitude") && body.has("longitude")) {
                                            responseJson.put("latitude", body.get("latitude").asText());
                                            responseJson.put("longitude", body.get("longitude").asText());
                                        }
                                        
                                        String jsonResponse = objectMapper.writeValueAsString(responseJson);

                                        // Send to all clients subscribed to the paths topic
                                        webSocketService.sendMessage(WEBSOCKET_TOPIC, jsonResponse);
                                    } else {
                                        // Yol bulunamadığında hata mesajı gönder
                                        HashMap<String, Object> errorJson = new HashMap<>();
                                        errorJson.put("error", "Path could not be calculated");
                                        errorJson.put("restaurantId", restaurantId.toString());
                                        errorJson.put("status", "ERROR");
                                        
                                        if (body.has("requestId")) {
                                            errorJson.put("requestId", body.get("requestId").asText());
                                        }
                                        
                                        String errorResponse = objectMapper.writeValueAsString(errorJson);
                                        webSocketService.sendMessage(ERROR_TOPIC, errorResponse);
                                        System.err.println("Path could not be calculated for restaurant ID: " + restaurantId);
                                    }
                                }
                            }
                            // For 404 (queue not found) or 204 (no content), just continue polling
                            
                            // Wait before polling again to avoid overwhelming the queue service
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        } catch (Exception e) {
                            // Log error and continue polling
                            System.err.println("Error polling queue: " + e.getMessage());
                            webSocketService.sendMessage(ERROR_TOPIC, "Queue polling error: " + e.getMessage());
                            // Wait a bit longer after an error
                            Thread.sleep(2000);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Queue polling thread terminated: " + e.getMessage());
                } finally {
                    running.set(false);
                }
            });
            
            pollingThread.setDaemon(true);
            pollingThread.start();
            System.out.println("Queue polling started for queue: " + QUEUE_NAME);
        }
    }
    
    /**
     * Stop the queue polling thread
     */
    public void stopQueuePolling() {
        if (running.compareAndSet(true, false)) {
            pollingThread.interrupt();
            System.out.println("Queue polling stopped for queue: " + QUEUE_NAME);
        }
    }

    /**
     * Clean up resources when the service is shutting down
     */
    @PreDestroy
    public void shutdown() {
        stopQueuePolling();
    }
}
