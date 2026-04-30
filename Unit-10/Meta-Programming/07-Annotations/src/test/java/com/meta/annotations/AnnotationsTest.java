package com.meta.annotations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationsTest {

    // ---------------------------------------------------------------
    // Test fixtures
    // ---------------------------------------------------------------

    static class Person {
        @Required(message = "Name required")
        String name;

        @Range(min = 0, max = 120, message = "Age out of range")
        int age;

        @Pattern(regex = "\\d{3}-\\d{4}", message = "Phone must match NNN-NNNN")
        String phone;

        Person(String name, int age, String phone) {
            this.name  = name;
            this.age   = age;
            this.phone = phone;
        }
    }

    static class Item {
        @Required
        String label;

        @Range(min = 1, max = 999)
        int quantity;

        Item(String label, int quantity) {
            this.label    = label;
            this.quantity = quantity;
        }
    }

    // ---------------------------------------------------------------
    // @Required
    // ---------------------------------------------------------------

    @Test @DisplayName("@Required passes when field is non-null non-blank")
    void required_passesForValidString() {
        ValidationResult r = Validator.validate(new Person("Alice", 30, "555-1234"));
        assertTrue(r.isValid());
    }

    @Test @DisplayName("@Required fails for null")
    void required_failsForNull() {
        ValidationResult r = Validator.validate(new Person(null, 30, "555-1234"));
        assertFalse(r.isValid());
        assertTrue(r.violations().get(0).contains("name"));
    }

    @Test @DisplayName("@Required fails for blank string")
    void required_failsForBlank() {
        ValidationResult r = Validator.validate(new Person("   ", 30, "555-1234"));
        assertFalse(r.isValid());
    }

    @Test @DisplayName("@Required uses custom message")
    void required_customMessage() {
        ValidationResult r = Validator.validate(new Person(null, 30, "555-1234"));
        assertTrue(r.violations().get(0).contains("Name required"));
    }

    // ---------------------------------------------------------------
    // @Range
    // ---------------------------------------------------------------

    @Test @DisplayName("@Range passes for value within bounds")
    void range_passesInBounds() {
        ValidationResult r = Validator.validate(new Person("Bob", 25, "555-9999"));
        assertTrue(r.isValid());
    }

    @Test @DisplayName("@Range fails when value below min")
    void range_failsBelowMin() {
        ValidationResult r = Validator.validate(new Person("Bob", -1, "555-9999"));
        assertFalse(r.isValid());
        assertTrue(r.violations().stream().anyMatch(v -> v.contains("age")));
    }

    @Test @DisplayName("@Range fails when value above max")
    void range_failsAboveMax() {
        ValidationResult r = Validator.validate(new Person("Bob", 200, "555-9999"));
        assertFalse(r.isValid());
    }

    @Test @DisplayName("@Range passes at exact boundary values")
    void range_passesAtBoundary() {
        assertTrue(Validator.validate(new Person("C", 0,   "555-0000")).isValid());
        assertTrue(Validator.validate(new Person("C", 120, "555-0000")).isValid());
    }

    // ---------------------------------------------------------------
    // @Pattern
    // ---------------------------------------------------------------

    @Test @DisplayName("@Pattern passes for matching string")
    void pattern_passesForMatch() {
        ValidationResult r = Validator.validate(new Person("Eve", 22, "123-4567"));
        assertTrue(r.isValid());
    }

    @Test @DisplayName("@Pattern fails for non-matching string")
    void pattern_failsForNonMatch() {
        ValidationResult r = Validator.validate(new Person("Eve", 22, "bad-phone"));
        assertFalse(r.isValid());
        assertTrue(r.violations().stream().anyMatch(v -> v.contains("phone")));
    }

    @Test @DisplayName("@Pattern null field is skipped (no NPE)")
    void pattern_nullSkipped() {
        ValidationResult r = Validator.validate(new Person("Eve", 22, null));
        assertTrue(r.isValid(), "null phone should not trigger @Pattern (null check is @Required's job)");
    }

    // ---------------------------------------------------------------
    // Multiple violations
    // ---------------------------------------------------------------

    @Test @DisplayName("Multiple violations are all collected")
    void multipleViolations() {
        ValidationResult r = Validator.validate(new Person(null, -5, "bad"));
        assertFalse(r.isValid());
        assertEquals(3, r.violations().size(), "Expected 3 violations: Required, Range, Pattern");
    }

    @Test @DisplayName("isValid() returns true only when no violations")
    void isValid_onlyWhenClean() {
        assertTrue(Validator.validate(new Item("Widget", 50)).isValid());
        assertFalse(Validator.validate(new Item(null, 0)).isValid());
    }

    // ---------------------------------------------------------------
    // Annotation metadata (retained at runtime)
    // ---------------------------------------------------------------

    @Test @DisplayName("@Required is retained at runtime")
    void required_retainedAtRuntime() throws Exception {
        Field f = Person.class.getDeclaredField("name");
        assertNotNull(f.getAnnotation(Required.class));
    }

    @Test @DisplayName("@Range min/max values accessible at runtime")
    void range_attributesAccessible() throws Exception {
        Field f = Person.class.getDeclaredField("age");
        Range r = f.getAnnotation(Range.class);
        assertNotNull(r);
        assertEquals(0,   r.min(), 0.001);
        assertEquals(120, r.max(), 0.001);
    }

    @Test @DisplayName("@Pattern regex accessible at runtime")
    void pattern_regexAccessible() throws Exception {
        Field f = Person.class.getDeclaredField("phone");
        Pattern p = f.getAnnotation(Pattern.class);
        assertNotNull(p);
        assertEquals("\\d{3}-\\d{4}", p.regex());
    }
}
