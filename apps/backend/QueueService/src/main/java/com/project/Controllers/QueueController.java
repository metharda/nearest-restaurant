package com.project.Controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import com.project.QueueService.QueueService;
import org.springframework.messaging.handler.annotation.DestinationVariable; // Added import

@Controller
public class QueueController { // Renamed class from Controller to QueueController

    /**
     * Handles requests to create a new queue.
     * The client should send the desired queue name as the message payload.
     *
     * @param queueName The name of the queue to create.
     * @return A status message indicating success or failure.
     */
    @MessageMapping("/queue/create")
    @SendTo("/topic/queues/status")
    public String createQueue(String queueName) {
        try {
            QueueService.createQueue(queueName); // Assumes QueueService.createQueue(String) exists
            return "Queue '" + queueName + "' created successfully.";
        } catch (Exception e) {
            // Consider adding logging for the exception e.g. log.error("Error creating queue", e);
            return "Failed to create queue '" + queueName + "'. Reason: " + e.getMessage();
        }
    }

    /**
     * Handles messages to be enqueued to a specific queue.
     * The queue name is part of the destination path.
     *
     * @param queueName The name of the queue to which the message will be added.
     * @param message   The message content to enqueue.
     * @return A confirmation message or an error message.
     */
    @MessageMapping("/queue/enqueue/{queueName}")
    @SendTo("/topic/messages/{queueName}") // Dynamically routes response to a queue-specific topic
    public String enqueueMessage(@DestinationVariable String queueName, String message) {
        try {
            QueueService.enqueue(queueName, message); // Assumes QueueService.enqueue(String, String) exists
            return "Message added to queue '" + queueName + "': " + message;
        } catch (Exception e) {
            // Consider adding logging for the exception
            return "Failed to enqueue message to queue '" + queueName + "'. Reason: " + e.getMessage();
        }
    }

    /**
     * Handles requests to dequeue a message from a specific queue.
     * The client sends a message to this endpoint (payload can be ignored) to trigger a dequeue.
     * The dequeued message (or status) is sent to a queue-specific data topic.
     *
     * @param queueName The name of the queue from which to dequeue a message.
     * @return The dequeued message, or a status message if the queue is empty or an error occurs.
     */
    @MessageMapping("/queue/dequeue/{queueName}")
    @SendTo("/topic/data/{queueName}") // Dynamically routes dequeued data to a queue-specific topic
    public String dequeueMessage(@DestinationVariable String queueName) {
        try {
            String data = QueueService.dequeue(queueName); // Assumes QueueService.dequeue(String) exists
            if (data != null) {
                return data;
            } else {
                return "Queue '" + queueName + "' is empty or no data retrieved.";
            }
        } catch (Exception e) {
            // Consider adding logging for the exception
            return "Failed to dequeue from queue '" + queueName + "'. Reason: " + e.getMessage();
        }
    }
}
