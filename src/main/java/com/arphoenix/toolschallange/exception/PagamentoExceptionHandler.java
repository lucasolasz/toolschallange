package com.arphoenix.toolschallange.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseRecord> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponseRecord errorResponse = new ErrorResponseRecord(message, HttpStatus.BAD_REQUEST.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseRecord> handleConstraintViolation(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));

        ErrorResponseRecord errorResponse = new ErrorResponseRecord(message, HttpStatus.BAD_REQUEST.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseRecord> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException exception) {
        String message = "Requisição malformada. Verifique se os campos estão corretos";

        ErrorResponseRecord errorResponse = new ErrorResponseRecord(message, HttpStatus.BAD_REQUEST.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(TempoExcedidoRequisicaoException.class)
    public ResponseEntity<ErrorResponseRecord> handleTempoExcedido(TempoExcedidoRequisicaoException exception) {
        ErrorResponseRecord errorResponse = new ErrorResponseRecord(exception.getMessage(),
                HttpStatus.REQUEST_TIMEOUT.toString());
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }
}
