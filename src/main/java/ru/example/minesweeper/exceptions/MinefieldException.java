package ru.example.minesweeper.exceptions;

public class MinefieldException extends RuntimeException {
    public MinefieldException() {
    }

    public MinefieldException(String message) {
        super(message);
    }

    public MinefieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
