package com.reflect.annot;

import java.lang.annotation.*;

/**
 * Marks a field as non-nullable.
 * Validated at runtime by {@link Validator}.
 */
@Retention(RetentionPolicy.RUNTIME)   // keep metadata in the JVM after class loading
@Target(ElementType.FIELD)            // only valid on fields
public @interface NotNull {
    String message() default "must not be null";
}
