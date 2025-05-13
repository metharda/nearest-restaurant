package com.project.Queue;

public class Queue<T> {
    Node<T> head;
    Node<T> tail;
    int size;

    public Queue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }
    public void enqueue(T data) {
        Node<T> newNode = new Node<>(data);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    public T dequeue() {
        if (head == null) {
            return null;
        }
        T data = head.data;
        head = head.next;
        if (head != null) {
            head.prev = null;
        } else {
            tail = null;
        }
        size--;
        return data;
    }

    public T peek() {
        if (head == null) {
            return null;
        }
        return head.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }
    
}
