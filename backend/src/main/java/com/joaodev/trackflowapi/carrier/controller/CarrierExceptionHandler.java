package com.joaodev.trackflowapi.carrier.controller;

import com.joaodev.trackflowapi.carrier.service.CarrierNotFoundException;
import com.joaodev.trackflowapi.common.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CarrierExceptionHandler {

    @ExceptionHandler(CarrierNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CarrierNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }
}