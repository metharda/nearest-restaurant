package com.project.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.QueueService.QueueService;

@RestController
@RequestMapping("/api/queue")
public class QueueRestController {

    /**
     * Handles HTTP requests to enqueue messages
     * 
     * @param message The message content to enqueue
     * @param queueName The queue name from headers
     * @return A response with confirmation or error
     */
    @PostMapping("/enqueue")
    public ResponseEntity<String> enqueueMessage(
            @RequestBody String message,
            @RequestHeader("QueueName") String queueName) {
        
        try {
            // Create queue if it doesn't exist
            if (!QueueService.queueExists(queueName)) {
                QueueService.createQueue(queueName);
            }
            
            QueueService.enqueue(queueName, message);
            return ResponseEntity.ok("Message added to queue '" + queueName + "'");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to enqueue message: " + e.getMessage());
        }
    }
    
    /**
     * Handles HTTP requests to dequeue messages
     * 
     * @param queueName The queue name from headers
     * @return The dequeued message or status message
     */
    @GetMapping("/dequeue")
    public ResponseEntity<String> dequeueMessage(
            @RequestHeader("QueueName") String queueName) {
        
        try {
            String data = QueueService.dequeue(queueName);
            if (data != null) {
                return ResponseEntity.ok(data);
            } else {
                return ResponseEntity.ok("Queue '" + queueName + "' is empty or doesn't exist.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to dequeue from queue '" + queueName + "': " + e.getMessage());
        }
    }
    
    /**
     * Check if a queue exists
     * 
     * @param queueName The queue name from path
     * @return Status of the queue
     */
    @GetMapping("/exists/{queueName}")
    public ResponseEntity<Boolean> queueExists(@PathVariable String queueName) {
        return ResponseEntity.ok(QueueService.queueExists(queueName));
    }
}
