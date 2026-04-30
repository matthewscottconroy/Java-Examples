package com.functional.composition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CompositionTest {

    @Test
    @DisplayName("notBlank fails on an empty string")
    void notBlankFails() {
        assertFalse(Validator.notBlank("field").apply("").isValid());
    }

    @Test
    @DisplayName("notBlank passes a non-empty string")
    void notBlankPasses() {
        assertTrue(Validator.notBlank("field").apply("hello").isValid());
    }

    @Test
    @DisplayName("minLength fails when value is too short")
    void minLengthFails() {
        ValidationResult r = Validator.minLength("field", 5).apply("abc");
        assertFalse(r.isValid());
        assertTrue(r.errorMessage().contains("5"));
    }

    @Test
    @DisplayName("chain short-circuits on first failure")
    void chainShortCircuits() {
        Function<String, ValidationResult> v = Validator.chain(
                Validator.notBlank("f"),
                Validator.minLength("f", 10),
                Validator.maxLength("f", 5)  // would conflict, but never reached
        );
        ValidationResult r = v.apply("hi");
        assertTrue(r.errorMessage().contains("10"), "Should fail on minLength, not maxLength");
    }

    @Test
    @DisplayName("chain passes when all validators succeed")
    void chainAllPass() {
        Function<String, ValidationResult> v = Validator.chain(
                Validator.notBlank("username"),
                Validator.minLength("username", 3),
                Validator.maxLength("username", 20),
                Validator.matches("username", "[a-zA-Z0-9_]+", "invalid chars")
        );
        assertTrue(v.apply("alice_chen").isValid());
    }

    @Test
    @DisplayName("then composes two validators sequentially")
    void thenComposition() {
        Function<String, ValidationResult> v = Validator.then(
                Validator.notBlank("f"),
                Validator.minLength("f", 4)
        );
        assertFalse(v.apply("").isValid(),   "blank should fail first check");
        assertFalse(v.apply("hi").isValid(), "short should fail second check");
        assertTrue(v.apply("hello").isValid());
    }

    @Test
    @DisplayName("regex validator catches invalid email")
    void regexValidator() {
        Function<String, ValidationResult> emailV = Validator.matches(
                "email", "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", "invalid");
        assertFalse(emailV.apply("notanemail").isValid());
        assertTrue(emailV.apply("a@b.com").isValid());
    }
}
