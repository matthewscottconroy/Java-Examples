package com.reflect.annot;

import java.lang.annotation.*;

/**
 * Maps a field to a database column name — like a minimal {@code @Column} from JPA.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
    String name();
    boolean nullable() default true;
}
