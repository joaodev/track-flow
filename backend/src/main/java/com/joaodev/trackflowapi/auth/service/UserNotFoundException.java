package com.joaodev.trackflowapi.auth.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class UserNotFoundException extends RuntimeException implements ApiException {
    public UserNotFoundException(Long id) {
        super("No user found with id " + id);
    }

    @Override
    public String getErrorCode() {
        return "USER_NOT_FOUND";
    }
}
