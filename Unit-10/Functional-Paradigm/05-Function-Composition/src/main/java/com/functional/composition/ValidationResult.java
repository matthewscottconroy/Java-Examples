package com.functional.composition;

/**
 * The output of a validation step — either a success carrying the validated
 * value, or a failure carrying the error message.
 *
 * <p>This is the data flowing through the composition pipeline.
 *
 * @param value        the validated string (null on failure)
 * @param errorMessage empty string on success, message on failure
 */
public record ValidationResult(String value, String errorMessage) {

    public static ValidationResult success(String value) {
        return new ValidationResult(value, "");
    }

    public static ValidationResult failure(String message) {
        return new ValidationResult(null, message);
    }

    public boolean isValid() { return errorMessage.isEmpty(); }

    @Override
    public String toString() {
        return isValid() ? "OK(\"" + value + "\")" : "FAIL(\"" + errorMessage + "\")";
    }
}
