package com.project.repository;

import org.springframework.stereotype.Repository;
import com.project.datastructures.HashTable;
import com.project.entity.Auth;
import com.project.datastructures.Linkedlist;

@Repository
public class AuthRepository {
    private final HashTable table = new HashTable(10);
    int current_id = 1;

    public int get_current_id() {
        int temp = this.current_id;
        this.current_id++;
        return temp;
    }

    public Linkedlist<Auth> findAll() {
        return new Linkedlist<>(table.values());
    }

    public Auth findbyid(int id) {
        return table.searchbyid(id);
    }

    public Auth save(Auth auth) {
        table.put(auth);
        return auth;
    }

    public Boolean existsByUsernameAndPassword(String username, String password) {
        return table.searchbyUsernameAndPassword(username, password) != null ? true : false;
    }

    public Boolean existsByUsername(String username) {
        return table.searchbyUsername(username) != null ? true : false;
    }

    public int fetch_userid_by_username(String username) {
        return table.searchbyUsername(username).getId();
    }

    public Auth fetch_user_by_userid(int user_id) {
        return table.searchbyid(user_id);
    }
}
