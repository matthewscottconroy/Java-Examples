package com.meta.annotations;

import java.util.*;

/**
 * Collects constraint violations from a single validation pass.
 */
public final class ValidationResult {

    private final List<String> violations = new ArrayList<>();

    void addViolation(String fieldName, String message) {
        violations.add(fieldName + ": " + message);
    }

    public boolean isValid() { return violations.isEmpty(); }

    public List<String> violations() { return Collections.unmodifiableList(violations); }

    @Override
    public String toString() {
        return isValid() ? "VALID" : "INVALID " + violations;
    }
}
