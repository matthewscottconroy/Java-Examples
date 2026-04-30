package com.meta.annotations;

import java.lang.annotation.*;

/**
 * Validates a String field against a regular expression.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Pattern {
    String regex();
    String message() default "Value does not match pattern";
}
