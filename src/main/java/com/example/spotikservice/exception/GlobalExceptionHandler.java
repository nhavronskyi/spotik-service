package com.example.spotikservice.exception;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserUnauthorizedException.class)
    public ResponseEntity<String> handleUserExistsException(UserUnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.SC_UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnknownException() {
        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Something went wrong");
    }
}
