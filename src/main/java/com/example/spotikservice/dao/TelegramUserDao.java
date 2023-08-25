package com.example.spotikservice.dao;


import java.util.List;

public interface TelegramUserDao {
    List<String> getAllUsersEmailWithActiveStatus();
}
