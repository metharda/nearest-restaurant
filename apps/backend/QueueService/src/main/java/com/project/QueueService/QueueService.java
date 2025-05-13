package com.project.QueueService;

import java.util.HashMap;
import com.project.Queue.Queue;

public class QueueService {
    static HashMap<String, Queue<String>> Queues = new HashMap<String, Queue<String>>();
    private QueueService() {
    }

    public static Queue<String> getQueue(String queueName) {
        if (Queues.containsKey(queueName)) {
            return Queues.get(queueName);
        } else {
            Queue<String> newQueue = new Queue<String>();
            Queues.put(queueName, newQueue);
            return newQueue;
        }
    }

    public static void enqueue(String queueName, String data) {
        Queue<String> queue = getQueue(queueName);
        queue.enqueue(data);
    }

    public static String dequeue(String queueName) {
        Queue<String> queue = getQueue(queueName);
        return queue.dequeue();
    }

    public static String peek(String queueName) {
        Queue<String> queue = getQueue(queueName);
        return queue.peek();
    }
    
}
