package com.example.spotikservice.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    String authorize();

    int setAccessToken(String code, HttpServletResponse response);

    void setAccessToken(HttpServletRequest request);
}
