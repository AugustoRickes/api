package com.itau.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.itau.api.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        // This is a catch-all for any other exceptions, returning a 500 Internal Server Error
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex){
    Map<String,Object> body = new LinkedHashMap<>();
    body.put("timestamp",LocalDateTime.now());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error","Validation Error");


    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error->{
        String fieldName = ((FieldError)error).getField();
        String errorMessage = error.getDefaultMessage();
        fieldErrors.put(fieldName,errorMessage);
    });

    body.put("messages", fieldErrors);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        
        return new ResponseEntity<>(body, status);
    }
}
