package com.example.spotikservice.exception;

public class UserUnauthorizedException extends RuntimeException {
    public UserUnauthorizedException() {
        super();
    }

    public UserUnauthorizedException(String message) {
        super(message);
    }
}