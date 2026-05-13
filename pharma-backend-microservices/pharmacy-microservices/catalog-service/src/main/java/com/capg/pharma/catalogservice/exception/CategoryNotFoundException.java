package com.capg.pharma.catalogservice.exception;

/** Thrown when a category ID is not found. Results in 404. */
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) { super(message); }
}
