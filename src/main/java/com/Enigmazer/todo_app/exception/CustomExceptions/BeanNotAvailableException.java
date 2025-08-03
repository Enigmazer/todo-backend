package com.Enigmazer.todo_app.exception.CustomExceptions;

public class BeanNotAvailableException extends RuntimeException {
    public BeanNotAvailableException(String message) {
        super(message);
    }
}
