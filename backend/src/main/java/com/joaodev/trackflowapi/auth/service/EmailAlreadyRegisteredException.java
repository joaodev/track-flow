package com.joaodev.trackflowapi.auth.service;

import com.joaodev.trackflowapi.common.error.ApiException;

public class EmailAlreadyRegisteredException extends RuntimeException implements ApiException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email already registered: " + email);
    }

    @Override
    public String getErrorCode() {
        return "EMAIL_ALREADY_REGISTERED";
    }
}
