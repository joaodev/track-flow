package com.joaodev.trackflowapi.auth.service;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("No user found with id " + id);
    }
}
