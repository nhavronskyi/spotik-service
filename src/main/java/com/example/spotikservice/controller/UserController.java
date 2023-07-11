package com.example.spotikservice.controller;

import com.example.spotikservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
public class UserController {
    private final AuthService service;

    @GetMapping
    public void getAuthorizeUrl(HttpServletResponse response) throws IOException {
        response.sendRedirect(service.authorize());
    }
}
