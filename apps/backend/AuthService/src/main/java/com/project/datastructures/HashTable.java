package com.project.datastructures;

import com.project.entity.Auth;

public class HashTable {
    public Linkedlist<Auth>[] table;
    public int size;

    @SuppressWarnings("unchecked")
    public HashTable(int size){
        this.size = size;
        this.table = (Linkedlist<Auth>[]) new Linkedlist[size];
    }

    public void put(Auth auth){
        int index = auth.getId() % size;
        if(table[index] == null){
            table[index] = new Linkedlist<>();
        }
        table[index].add(auth);
    }

    public Auth searchbyid(int id){
        int index = id % size;
        if(table[index] == null){
            return null;
        }
        Node<Auth> current = table[index].head;
        while(current != null){
            if(current.value.getId() == id){
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public Auth searchbyUsernameAndPassword(String username, String password) {
        for(int i = 0; i<size ; ++i){
            if(table[i] == null){
                continue;
            }
            Node<Auth> current = table[i].head;
            while(current != null){
                if(current.value.getUsername().equals(username) && current.value.getPassword().equals(password)){
                    return current.value;
                }
                current = current.next;
            }
        }
        return null;
    }

    public Auth searchbyUsername(String username) {
        for(int i = 0; i<size ; ++i){
            if(table[i] == null){
                continue;
            }
            Node<Auth> current = table[i].head;
            while(current != null){
                if(current.value.getUsername().equals(username)){
                    return current.value;
                }
                current = current.next;
            }
        }
        return null;
    }

    public Linkedlist<Auth> values(){
        Linkedlist<Auth> list = new Linkedlist<>();
        for(int i = 0; i<size; ++i){
            if(table[i] != null){
                Node<Auth> current = table[i].head;
                while(current != null){
                    list.add(current.value);
                    current = current.next;
                }
            }
        }
        return list;
    }

    public void print(){
        for(int i = 0; i<size; ++i){
            if(table[i] != null){
                System.out.println("Index: " + i);
                table[i].print();
            }
        }
    }
}
