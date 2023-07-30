package com.example.spotikservice.controller;

import com.example.spotikservice.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @GetMapping("auth")
    public void getAuthorizeUrl(HttpServletResponse response) {
        try {
            response.sendRedirect(service.authorize());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @GetMapping
    public int setAccessToken(@RequestParam String code, HttpServletResponse response) {
        return service.setAccessToken(code, response);
    }
}
