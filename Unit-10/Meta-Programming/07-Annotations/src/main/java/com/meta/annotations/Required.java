package com.meta.annotations;

import java.lang.annotation.*;

/**
 * Marks a field as required — the validator rejects null and blank strings.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Required {
    String message() default "Field is required";
}
