package com.Enigmazer.todo_app.exception;

import com.Enigmazer.todo_app.exception.CustomExceptions.DuplicateResourceException;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// this class is for handling exceptions manually
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class) // Fallback for all other exceptions that I haven’t handled
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return generateResponse("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(DuplicateResourceException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleBadFormat(IllegalStateException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(DisabledException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthException(AuthenticationException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleBadFormat(JwtException ex) {
        return generateResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, String>> generateResponse(String msg, HttpStatus status) {
        Map<String, String> body = new HashMap<>();
        body.put("error", status.getReasonPhrase());
        body.put("message", msg);
        return new ResponseEntity<>(body, status);
    }
}
