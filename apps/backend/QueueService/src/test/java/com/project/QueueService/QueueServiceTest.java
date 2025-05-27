package com.project.QueueService;

import com.project.Queue.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import com.project.datastructures.HashMap;

public class QueueServiceTest {

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Reset the Queues map before each test using reflection
        Field field = QueueService.class.getDeclaredField("Queues");
        field.setAccessible(true);
        field.set(null, new HashMap<String, Queue<String>>());
    }

    @Test
    void testCreateQueue() {
        QueueService.createQueue("testQueue1");
        assertNotNull(QueueService.getQueue("testQueue1"), "Queue should be created and retrievable.");

        // Test creating the same queue again (should not throw error, just print to console)
        QueueService.createQueue("testQueue1");
        assertNotNull(QueueService.getQueue("testQueue1"), "Queue should still exist after attempting to create it again.");
    }

    @Test
    void testEnqueue() {
        String queueName = "enqueueTestQueue";
        QueueService.createQueue(queueName);
        QueueService.enqueue(queueName, "message1");
        QueueService.enqueue(queueName, "message2");

        Queue<String> queue = QueueService.getQueue(queueName);
        assertNotNull(queue, "Queue should exist.");
        assertEquals("message1", queue.peek(), "First message enqueued should be at the front.");
    }

    @Test
    void testDequeue() {
        String queueName = "dequeueTestQueue";
        QueueService.createQueue(queueName);
        QueueService.enqueue(queueName, "msg1");
        QueueService.enqueue(queueName, "msg2");

        assertEquals("msg1", QueueService.dequeue(queueName), "Dequeue should return the first message.");
        assertEquals("msg2", QueueService.dequeue(queueName), "Dequeue should return the second message.");
        assertNull(QueueService.dequeue(queueName), "Dequeue on an empty queue should return null.");
    }

    @Test
    void testPeek() {
        String queueName = "peekTestQueue";
        QueueService.createQueue(queueName);
        QueueService.enqueue(queueName, "peekMsg");

        assertEquals("peekMsg", QueueService.peek(queueName), "Peek should return the front message without removing it.");
        assertEquals("peekMsg", QueueService.peek(queueName), "Peek again should return the same message.");
        assertNotNull(QueueService.getQueue(queueName).peek(), "Queue should not be empty after peek.");

        String emptyQueueName = "peekEmptyQueue";
        QueueService.createQueue(emptyQueueName);
        assertNull(QueueService.peek(emptyQueueName), "Peek on an empty queue should return null.");
    }

    @Test
    void testGetQueue_createsNewQueueIfNotExists() {
        String newQueueName = "dynamicQueue";
        Queue<String> queue = QueueService.getQueue(newQueueName);
        assertNotNull(queue, "getQueue should create a new queue if it doesn't exist.");
        assertTrue(QueueService.getQueue(newQueueName).isEmpty(), "Newly created queue should be empty.");
    }

    @Test
    void testDequeue_nonExistentQueue() {
        assertNull(QueueService.dequeue("nonExistentQueue"), "Dequeue on a non-existent queue should return null.");
    }

    @Test
    void testPeek_nonExistentQueue() {
        assertNull(QueueService.peek("nonExistentQueue"), "Peek on a non-existent queue should return null.");
    }
}
