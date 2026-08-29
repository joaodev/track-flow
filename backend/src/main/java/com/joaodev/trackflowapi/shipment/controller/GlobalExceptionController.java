package com.joaodev.trackflowapi.shipment.controller;

import com.joaodev.trackflowapi.common.error.ErrorResponse;
import com.joaodev.trackflowapi.shipment.service.ShipmentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler (ShipmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ShipmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler (MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_FAILED", "Validation failed", fieldErrors));
    }
}
