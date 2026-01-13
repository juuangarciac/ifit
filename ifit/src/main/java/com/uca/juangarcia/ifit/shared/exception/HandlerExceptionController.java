package com.uca.juangarcia.ifit.shared.exception;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class HandlerExceptionController {

    @ExceptionHandler({ 
        DataIntegrityViolationException.class,
        IllegalArgumentException.class,
        HttpMessageNotReadableException.class,
        NullPointerException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        "Error: " + ex.getClass().getSimpleName(),
                        400, 
                        LocalDateTime.now().toString(),
                        ex.getMessage()));
    }

    @ExceptionHandler({
        NoResourceFoundException.class,
        EmailNotFoundException.class,
        UserIdNotFoundException.class,
        CoachModelTypeNotFoundException.class,
        ExperienceLevelNotFoundException.class,
        QuestionnaireQuestionNotFoundException.class,
        QuestionnaireNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        "Error: " + ex.getClass().getSimpleName(),
                        404, 
                        LocalDateTime.now().toString(),
                        ex.getMessage()));
    }

    @ExceptionHandler({
        EmailAlreadyExistsException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictException(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        "Error: " + ex.getClass().getSimpleName(),
                        409, 
                        LocalDateTime.now().toString(),
                        ex.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse(
                        "Error: " + ex.getClass().getSimpleName(),
                        405, // ✅ Consistente con METHOD_NOT_ALLOWED
                        LocalDateTime.now().toString(),
                        ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        "Error: " + ex.getClass().getSimpleName(),
                        500, 
                        LocalDateTime.now().toString(),
                        ex.getMessage()));
    }
}