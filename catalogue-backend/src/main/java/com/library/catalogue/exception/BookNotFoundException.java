package com.library.catalogue.exception;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(UUID id) {
        super("Book not found with id: " + id);
    }

    public BookNotFoundException(String isbn) {
        super("Book not found with ISBN: " + isbn);
    }
}
