package com.example.spotikservice.dao;

import com.example.spotikservice.entities.User;

import java.util.List;
import java.util.Optional;


public interface UserDao {
    void save(User user);

    Optional<User> findById(String userId);

    List<User> getAllUsersByEmails(List<String> emails);
}
