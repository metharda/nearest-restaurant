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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import com.project.utils.Dijkstra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.datastructures.Graph;
import com.project.datastructures.HashMap;
import com.project.datastructures.Linkedlist;
import com.project.datastructures.Node;
import com.project.entity.Location;

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
    private static final ObjectMapper objectMapper = new ObjectMapper();      // Thread havuzu için ExecutorService ekliyoruz - limit the number of threads to prevent memory issues
    private static final int MAX_CALCULATION_THREADS = 2; // Reduced from 5 to 2 to limit memory usage
    private final ExecutorService calculationExecutor = Executors.newFixedThreadPool(MAX_CALCULATION_THREADS);

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
                                if (responseBody != null && !responseBody.equals("null") && !responseBody.isEmpty() && !responseBody.trim().equals("{}")) {
                                    Graph graph = Graph.getInstance();
                                    JsonNode body = objectMapper.readTree(responseBody);
                                    
                                    // Check if restaurantId exists in the JSON
                                    if (!body.has("restaurantId")) {
                                        System.err.println("Error: restaurantId field missing in request: " + responseBody);
                                        HashMap<String, Object> errorJson = new HashMap<>();
                                        errorJson.put("error", "restaurantId field missing in request");
                                        errorJson.put("status", "ERROR");
                                        if (body.has("requestId")) {
                                            errorJson.put("requestId", body.get("requestId").asText());
                                        }
                                        String errorResponse = objectMapper.writeValueAsString(errorJson);
                                        webSocketService.sendMessage(ERROR_TOPIC, errorResponse);
                                        continue;
                                    }
                                    
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
                                        continue;                                    }
                                    
                                    // A* algoritmasını ayrı bir thread'de çalıştır
                                    final JsonNode requestBody = body;
                                    final Long requestRestaurantId = restaurantId;
                                    final Location requestRestaurant = restaurant;
                                    final Graph requestGraph = graph;
                                    calculationExecutor.submit(() -> {
                                        try {
                                            System.out.println("Starting path calculation for restaurant: " + requestRestaurantId);

                                            // A* algoritması ile yol hesapla
                                            
                                            Linkedlist<Location> path = Dijkstra.findPath(requestGraph, requestGraph.getUser_location(), requestRestaurant);                                            if (path != null) {
                                                try {
                                                    int pathLength = path.length();
                                                    if (pathLength > 0) {
                                                        // Use a more efficient approach that doesn't require creating a large array upfront
                                                        // Collect only necessary location data into a simplified array
                                                        HashMap<String, Object> responseJson = new HashMap<>();
                                                        responseJson.put("requestId", requestBody.has("requestId") ? requestBody.get("requestId").asText() : "unknown");
                                                        responseJson.put("restaurantId", requestRestaurantId.toString());
                                                        responseJson.put("status", "COMPLETED");
                                                        
                                                        // Eğer latitude ve longitude bilgileri varsa ekle
                                                        if (requestBody.has("latitude") && requestBody.has("longitude")) {
                                                            responseJson.put("latitude", requestBody.get("latitude").asText());
                                                            responseJson.put("longitude", requestBody.get("longitude").asText());
                                                        }
                                                        
                                                        // Process path in chunks to avoid creating one large array
                                                        Location[] path_locations = new Location[pathLength];
                                                        Node<Location> current = path.head;
                                                        for (int i = 0; i < pathLength; i++) {
                                                            path_locations[i] = current.value;
                                                            current = current.next;
                                                        }
                                                        
                                                        responseJson.put("path", path_locations);
                                                        
                                                        String jsonResponse = objectMapper.writeValueAsString(responseJson);
                                                        
                                                        // Send to all clients subscribed to the paths topic
                                                        webSocketService.sendMessage(WEBSOCKET_TOPIC, jsonResponse);
                                                        System.out.println("Path calculation completed for restaurant: " + requestRestaurantId);
                                                    } else {
                                                        throw new Exception("Path is empty");
                                                    }
                                                } catch (Exception e) {
                                                    System.err.println("Error processing path data: " + e.getMessage());
                                                    e.printStackTrace();
                                                } finally {
                                                    // Explicitly clean up to help garbage collection
                                                    path = null;
                                                }
                                            } else {
                                                // Yol bulunamadığında hata mesajı gönder
                                                HashMap<String, Object> errorJson = new HashMap<>();
                                                errorJson.put("error", "Path could not be calculated");
                                                errorJson.put("restaurantId", requestRestaurantId.toString());
                                                errorJson.put("status", "ERROR");

                                                if (requestBody.has("requestId")) {
                                                    errorJson.put("requestId", requestBody.get("requestId").asText());
                                                }

                                                String errorResponse = objectMapper.writeValueAsString(errorJson);
                                                webSocketService.sendMessage(ERROR_TOPIC, errorResponse);
                                                System.err.println("Path could not be calculated for restaurant ID: " + requestRestaurantId);
                                            }
                                        } catch (Exception e) {
                                            System.err.println("Error in path calculation thread: " + e.getMessage());
                                            e.printStackTrace();
                                        }
                                    });                                }
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
     * Safely restart the queue polling
     * This can be called from within the polling thread or externally
     */
    public void restartQueuePolling() {
        System.out.println("Attempting to restart queue polling...");

        // Instead of reusing the same method, we'll create a dedicated restart method
        // that handles the thread context differently
        if (Thread.currentThread() == pollingThread) {
            System.out.println("Restart called from within polling thread");
            // Mark that we need a restart, but don't do it from here
            final Thread currentPollingThread = pollingThread;

            // Create a completely new thread to handle the restart
            new Thread(() -> {
                try {
                    // Wait a bit to ensure the original thread can complete its current task
                    System.out.println("Waiting for current polling thread to finish...");
                    Thread.sleep(1000);

                    // Force stop the current thread if it's still running
                    if (currentPollingThread.isAlive()) {
                        running.set(false);
                        currentPollingThread.interrupt();
                        System.out.println("Force stopped the previous polling thread");

                        // Give it time to actually stop
                        Thread.sleep(500);
                    }

                    // Start a fresh polling thread
                    System.out.println("Starting new polling thread...");
                    startQueuePolling();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Restart thread was interrupted: " + e.getMessage());
                }
            }).start();
        } else {
            // Normal restart from outside the polling thread
            System.out.println("Restart called from outside polling thread");
            stopQueuePolling();

            try {
                // Give the thread time to stop
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            startQueuePolling();
        }
    }

    /**
     * Clean up resources when the service is shutting down
     */
    @PreDestroy
    public void shutdown() {
        stopQueuePolling();

        // ExecutorService'i de düzgün şekilde kapat
        calculationExecutor.shutdown();
        try {
            if (!calculationExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                calculationExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            calculationExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
