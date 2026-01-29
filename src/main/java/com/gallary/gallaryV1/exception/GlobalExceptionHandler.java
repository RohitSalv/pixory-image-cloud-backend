package com.gallary.gallaryV1.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        // Log the full stack trace to the console for detailed debugging
        ex.printStackTrace();
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
