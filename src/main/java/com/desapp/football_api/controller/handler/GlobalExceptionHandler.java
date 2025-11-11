package com.desapp.football_api.controller.handler;

import com.desapp.football_api.exceptions.generic.BadRequestException;
import com.desapp.football_api.exceptions.generic.NotFoundException;
import com.desapp.football_api.exceptions.generic.UnauthorizedException;
import com.desapp.football_api.model.player.StatsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
//    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({NotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleResourceNotFound(Exception ex) {
        log.error("Resource not found: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        log.error("Bad Request: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("Bad Request: {}", ex.getMessage());

        String message;
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(StatsType.class)) {
            message = "Invalid value for 'type'. Allowed: Current, Historical.";
        } else if (ex.getRequiredType() != null && ex.getRequiredType().equals(java.time.LocalDate.class)) {
            String paramName = ex.getName();
            Object value = ex.getValue();
            Throwable cause = ex.getCause();
            if (cause instanceof java.time.format.DateTimeParseException) {
                message = "Parameter '" + paramName + "' with value '" + value + "' is not a valid date. Expected format: yyyy-MM-dd.";
            } else {
                message = "Parameter '" + paramName + "' is invalid. Expected a date in yyyy-MM-dd format.";
            }
        } else {
            String expectedType = (ex.getRequiredType() != null)
                    ? ex.getRequiredType().getSimpleName()
                    : "Unknown";
            message = "Invalid parameter: " + ex.getName() + ". Expected type: " + expectedType;
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(UnauthorizedException ex) {
        log.error("Unauthorized of: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Error reference {} - {}",
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Reference: " + ex.getLocalizedMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleArgumentNotValid(MethodArgumentNotValidException ex) {
        HashMap<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error on request",
                LocalDateTime.now(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Acceso denegado: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "No tiene permisos para acceder a este recurso.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}