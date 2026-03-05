package com.arphoenix.toolschallange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PagamentoExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleNotFound(NotFoundException exception) {
        ErrorResponseRecord errorResponse = new ErrorResponseRecord(exception.getMessage(),
                HttpStatus.NOT_FOUND.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseRecord> handleIllegalArgument(IllegalArgumentException exception) {
        ErrorResponseRecord errorResponse = new ErrorResponseRecord(exception.getMessage(),
                HttpStatus.BAD_REQUEST.toString());
        // Para IDs nulos ou argumentos inválidos, usamos BAD_REQUEST (400)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
