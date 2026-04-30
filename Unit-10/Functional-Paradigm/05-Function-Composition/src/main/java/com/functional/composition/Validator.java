package com.functional.composition;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility class for building composable validation functions.
 *
 * <p>Each factory method returns a {@code Function<String, ValidationResult>}.
 * Individual validators are composed with {@link Function#andThen} to form a
 * pipeline: the output of one step becomes the input of the next.
 */
public final class Validator {

    private Validator() {}

    /**
     * A validator that fails if the field is blank.
     */
    public static Function<String, ValidationResult> notBlank(String fieldName) {
        return value -> value == null || value.isBlank()
                ? ValidationResult.failure(fieldName + " must not be blank")
                : ValidationResult.success(value);
    }

    /**
     * A validator that enforces a minimum length.
     */
    public static Function<String, ValidationResult> minLength(String fieldName, int min) {
        return value -> value.length() < min
                ? ValidationResult.failure(fieldName + " must be at least " + min + " characters")
                : ValidationResult.success(value);
    }

    /**
     * A validator that enforces a maximum length.
     */
    public static Function<String, ValidationResult> maxLength(String fieldName, int max) {
        return value -> value.length() > max
                ? ValidationResult.failure(fieldName + " must be at most " + max + " characters")
                : ValidationResult.success(value);
    }

    /**
     * A validator that applies a regex pattern.
     */
    public static Function<String, ValidationResult> matches(String fieldName, String regex,
                                                              String message) {
        return value -> value.matches(regex)
                ? ValidationResult.success(value)
                : ValidationResult.failure(fieldName + " " + message);
    }

    /**
     * Compose two validators: run {@code first}, then {@code second} only if
     * the first succeeded.
     *
     * <p>This is manual composition. Java's {@code Function::andThen} applies
     * the second function unconditionally; here we short-circuit on failure.
     */
    public static Function<String, ValidationResult> then(
            Function<String, ValidationResult> first,
            Function<String, ValidationResult> second) {
        return value -> {
            ValidationResult r = first.apply(value);
            return r.isValid() ? second.apply(value) : r;
        };
    }

    /**
     * Chain an arbitrary number of validators left-to-right, short-circuiting
     * on the first failure.
     */
    @SafeVarargs
    public static Function<String, ValidationResult> chain(
            Function<String, ValidationResult>... validators) {
        return value -> {
            for (var v : validators) {
                ValidationResult r = v.apply(value);
                if (!r.isValid()) return r;
            }
            return ValidationResult.success(value);
        };
    }
}
