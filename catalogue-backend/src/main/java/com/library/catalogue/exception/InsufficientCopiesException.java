package com.library.catalogue.exception;

public class InsufficientCopiesException extends RuntimeException {
    public InsufficientCopiesException(String title) {
        super("No available copies of book: " + title);
    }
}
