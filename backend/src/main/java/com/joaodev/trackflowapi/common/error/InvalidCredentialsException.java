package com.joaodev.trackflowapi.common.error;

public class InvalidCredentialsException extends RuntimeException implements ApiException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    @Override
    public String getErrorCode() {
        return "INVALID_CREDENTIALS";
    }
}
