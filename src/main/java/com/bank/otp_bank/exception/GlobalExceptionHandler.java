package com.bank.otp_bank.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UniversalException.class)
    public ResponseEntity<ErrorResponse> handleApiException(UniversalException ex) {
        ErrorResponse response = new ErrorResponse(
            ex.getStatus().getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Некорректный JSON или неверный формат данных",
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }




    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message,
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NotFoundAccountException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundAccountException(NotFoundAccountException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MaxCardsException.class)
    public ResponseEntity<ErrorResponse> handleMaxCardsException(MaxCardsException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UnderFundedException.class)
    public ResponseEntity<ErrorResponse> handleUnderFundedException(UnderFundedException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(response);
    }

    @ExceptionHandler(InvalidCardNumberException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCardNumberException(InvalidCardNumberException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidCardNumberFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCardNumberFormatException(InvalidCardNumberFormatException ex) {
        ErrorResponse response = new ErrorResponse(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
