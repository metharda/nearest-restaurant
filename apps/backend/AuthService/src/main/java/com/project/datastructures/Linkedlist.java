package com.project.datastructures;

public class Linkedlist<T> {
    public Node<T> head;

    public Linkedlist() {
    }

    public Linkedlist(Linkedlist<T> list) {
        if(list.head == null){
            this.head = null;
        }
        else{
            this.head = new Node<T>(list.head.value);
            Node<T> current = this.head;
            Node<T> next = this.head.next;
            while(next != null){
                current.next = new Node<T>(next.value);
                current = current.next;
                next = next.next;
            }
        }
    }

    public void add(T value) {
        if (this.head == null) {
            this.head = new Node<T>(value, null);
            return;
        }
        Node<T> current = this.head;
        while (current.next != null) {
            current = current.next;
        }
        current.next = new Node<T>(value, null);
    }

    public void addFirst(T value){
        Node<T> newhead = new Node<T>(value, head);
        head = newhead;
    }

    public void removebyvalue(T value) {
        Node<T> current = head;
        Node<T> prev = null;
        if(current == null){
            return;
        }
        else if (head.value == value) {
            head = head.next;
            return;
        }
        while (current.next != null) {
            if (current.value == value) {
                if(prev != null){
                    prev.next = current.next;
                }
                return;
            }
            prev = current;
            current = current.next;
        }
        if(current.value == value){
            if(prev != null){
                prev.next = current.next;
            }
        }
    }

    public void removebyindex(int index) {
        Node<T> current = this.head;
        Node<T> prev = null;
        if(head == null){
            System.out.println("Index out of bounds");
            return;
        }
        else if(index == 0){
            head = head.next;
            return;
        }
        for (int i = 0; i < index ; ++i){
            if(current.next == null){
                System.out.println("Index out of bounds");
                return;
            }
            prev = current;
            current = current.next;
        }
        if(prev != null){
            prev.next = current.next;
        }
    }

    public void insert(T value, int index) {
        if (this.head == null){
            this.head = new Node<T>(value, null);
            return;
        }
        else if(index == 0){
            Node<T> newhead = new Node<T>(value, head);
            head = newhead;
            return;
        }
        Node<T> current = this.head;
        for (int i = 1; i < index; i++) {
            if (current.next == null) {
                System.out.println("Index out of bounds");
                return;
            }
            current = current.next;
        }
        current.next = new Node<T>(value, current.next);
    }

    public void set(T value, int index) {
        if (this.head == null) {
            System.out.println("Node is empty");
            return;
        }
        Node<T> current = this.head;
        for (int i = 0; i < index; i++) {
            if (current.next == null) {
                System.out.println("Index out of bounds");
                return;
            }
            current = current.next;
        }
        current.value = value;
    }

    public T get(int index) {
        Node<T> current = head;
        if (current == null) {
            System.out.println("Node is empty");
            return null;
        }
        for (int i = 0; i < index; i++) {
            if (current.next == null) {
                System.out.println("Index out of bounds");
                return null;
            }
            current = current.next;
        }
        return current.value;
    }

    public int length() {
        Node<T> current = head;
        int count = 0;
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }

    public int count(T value) {
        Node<T> current = this.head;
        int count = 0;
        while (current != null) {
            if (current.value == value) {
                count++;
            }
            current = current.next;
        }
        return count;
    }

    public void print() {
        Node<T> current = this.head;
        System.out.print("LinkedList : [");
        while (current != null) {
            System.out.print(current.value + ",");
            current = current.next;
        }
        System.out.print("null]\n");
    }
}
