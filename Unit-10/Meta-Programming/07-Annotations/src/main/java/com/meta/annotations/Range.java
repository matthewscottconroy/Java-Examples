package com.meta.annotations;

import java.lang.annotation.*;

/**
 * Constrains a numeric field (int, long, double) to [min, max].
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Range {
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
    String message() default "Value out of range";
}
