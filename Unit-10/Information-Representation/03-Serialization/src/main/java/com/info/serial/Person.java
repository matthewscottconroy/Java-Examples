package com.info.serial;

import java.io.Serializable;

/**
 * Simple value type used across all three serialization formats.
 * Implements {@link Serializable} so Java's built-in object stream can handle it.
 */
public record Person(String name, int age, String email) implements Serializable {
    private static final long serialVersionUID = 1L;
}
