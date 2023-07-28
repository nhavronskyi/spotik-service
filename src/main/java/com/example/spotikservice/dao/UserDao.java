package com.example.spotikservice.dao;

import com.example.spotikservice.entities.User;

import java.util.Optional;


public interface UserDao {
    void save(User user);

    Optional<User> findById(String userId);
}
