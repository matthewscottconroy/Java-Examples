package com.reflect.annot;

import java.lang.annotation.*;

/**
 * Constrains the length of a String field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Length {
    int min() default 0;
    int max() default Integer.MAX_VALUE;
    String message() default "length out of range";
}
