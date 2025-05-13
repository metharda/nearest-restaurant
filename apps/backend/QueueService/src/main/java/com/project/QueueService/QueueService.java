package com.project.QueueService;

import java.util.HashMap;
import com.project.Queue.Queue;

public class QueueService {
    static HashMap<String, Queue<String>> Queues = new HashMap<String, Queue<String>>();
    private QueueService() {
    }

    public static synchronized Queue<String> getQueue(String queueName) {
        if (Queues.containsKey(queueName)) {
            return Queues.get(queueName);
        } else {
            Queue<String> newQueue = new Queue<String>();
            Queues.put(queueName, newQueue);
            return newQueue;
        }
    }

    public static synchronized void createQueue(String queueName) {
        if (!Queues.containsKey(queueName)) {
            Queues.put(queueName, new Queue<String>());
        } else {
            System.out.println("Queue '" + queueName + "' already exists.");
        }
    }

    public static synchronized void enqueue(String queueName, String data) {
        Queue<String> queue = getQueue(queueName);
        queue.enqueue(data);
    }

    public static synchronized String dequeue(String queueName) {
        if (Queues.containsKey(queueName)) {
            Queue<String> queue = Queues.get(queueName);
            return queue.dequeue();
        } else {
            return null;
        }
    }

    public static synchronized String peek(String queueName) {
        if (Queues.containsKey(queueName)) {
            Queue<String> queue = Queues.get(queueName);
            return queue.peek();
        } else {
            return null;
        }
    }
    
}
