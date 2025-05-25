package com.project.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.lang.Math;

public class HashMap<K, V> implements Map<K, V> {
    private int size = 100;
    private Linkedlist<Entry<K, V>> entries;

    public HashMap() {
        this.entries = new Linkedlist<>();
    }

    public HashMap(int size) {
        this.size = size;
        this.entries = new Linkedlist<>();
    }

    public class HashEntry implements Entry<K, V> {
        private K key;
        private V value;

        public HashEntry() {
        }

        public HashEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }

        @Override
        public int hashCode() {
            int keyHash = (key == null) ? 0 : Math.floorMod(key.hashCode(), size);
            int valueHash = (value == null) ? 0 : Math.floorMod(value.hashCode(), size);
            return Math.floorMod(keyHash ^ valueHash, size);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Entry)) return false;
            Entry<?, ?> e = (Entry<?, ?>) o;
            return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }
    }

    @Override
    public V put(K key, V value) {
        Node<Entry<K, V>> current = entries.head;

        while(current != null) {
            if (Objects.equals(current.value.getKey(), key)) {
                return current.value.setValue(value);
            }

            current = current.next;
        }

        entries.add(new HashEntry(key, value));
        return null;
    }

    @Override
    public V get(Object key) {
        Node<Entry<K, V>> current = entries.head;

        while(current != null) {
            if (Objects.equals(current.value.getKey(), key)) {
                return current.value.getValue();
            }

            current = current.next;
        }

        return null;
    }

    @Override
    public V remove(Object key) {
        Node<Entry<K, V>> current = entries.head;
        Node<Entry<K, V>> prev = null;

        while(current != null) {
            if (Objects.equals(current.value.getKey(), key)) {
                V value = current.value.getValue();

                if (prev == null) {
                    entries.head = current.next;
                    current = null;
                }

                prev.next = current.next;
                current = null;
                return value;
            }
            
            prev = current;
            current = current.next;
        }

        return null;
    }

    @Override
    public int size() {
        return entries.length();
    }

    @Override
    public boolean isEmpty() {
        return entries.head == null;
    }

    @Override
    public boolean containsKey(Object key) {
        Node<Entry<K, V>> current = entries.head;

        while(current != null) {
            if (Objects.equals(current.value.getKey(), key)) {
                return true;
            }

            current = current.next;
        }

        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        Node<Entry<K, V>> current = entries.head;

        while(current != null) {
            if (Objects.equals(current.value.getValue(), value)) {
                return true;
            }

            current = current.next;
        }

        return false;
    }

    @Override
    public void clear() {
        entries.head = null;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        Node<Entry<K, V>> current = entries.head;

        while(current != null) {
            keys.add(current.value.getKey());
            current = current.next;
        }

        return keys;
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        Node<Entry<K, V>> current = entries.head;

        while(current != null) {
            values.add(current.value.getValue());
            current = current.next;
        }

        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        List<Entry<K, V>> list_entries = new ArrayList<>();
        Node<Entry<K, V>> current = entries.head;
        
        while(current != null) {
            list_entries.add(current.value);
            current = current.next;
        }
        
        return new HashSet<>(list_entries);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
}
