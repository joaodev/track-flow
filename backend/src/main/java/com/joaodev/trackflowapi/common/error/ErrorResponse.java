package com.joaodev.trackflowapi.common.error;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        LocalDateTime timestamp,
        String errorCode,
        String message,
        Map<String, String> fields
) {
    public static ErrorResponse of (String errorCode, String message) {
        return new ErrorResponse(LocalDateTime.now(), errorCode, message, null);
    }

    public static ErrorResponse of (String errorCode, String message, Map<String, String> fields) {
        return new ErrorResponse(LocalDateTime.now(), errorCode, message, fields);
    }
}
