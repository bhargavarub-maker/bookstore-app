package com.vone8.bookstore.common;

/** Thrown when a request clashes with the current state (e.g. out of stock). Becomes HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
