package com.joaodev.trackflowapi.product.controller;

import com.joaodev.trackflowapi.common.error.ErrorResponse;
import com.joaodev.trackflowapi.product.service.ProductNotFoundException;
import com.joaodev.trackflowapi.product.service.SkuAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ProductExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(SkuAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSku(SkuAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }
}
