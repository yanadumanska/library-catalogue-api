package com.library.catalogue.exception;

import java.util.UUID;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(UUID id) {
        super("Author not found with id: " + id);
    }
}
