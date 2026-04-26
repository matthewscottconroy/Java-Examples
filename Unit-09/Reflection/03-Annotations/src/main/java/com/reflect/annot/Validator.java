package com.reflect.annot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A minimal annotation-driven validator — the same pattern used by
 * Bean Validation (JSR 380, Hibernate Validator) and Spring's {@code @Valid}.
 *
 * <p>For each field on the target object:
 * <ol>
 *   <li>Read the field's runtime annotations via {@link Field#getDeclaredAnnotations()}.</li>
 *   <li>Apply the constraint logic.</li>
 *   <li>Collect all violation messages.</li>
 * </ol>
 *
 * <p>The validator never imports {@link User} — it works against any class
 * that uses these annotations.  That is the power of annotation-driven design.
 */
public class Validator {

    public record Violation(String field, String message) {
        @Override public String toString() { return field + ": " + message; }
    }

    public static List<Violation> validate(Object obj) {
        List<Violation> violations = new ArrayList<>();
        Class<?> cls = obj.getClass();

        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                continue;   // should not happen after setAccessible
            }

            // @NotNull
            if (field.isAnnotationPresent(NotNull.class) && value == null) {
                violations.add(new Violation(field.getName(),
                        field.getAnnotation(NotNull.class).message()));
            }

            // @Length (only meaningful for non-null Strings)
            if (field.isAnnotationPresent(Length.class) && value instanceof String str) {
                Length len = field.getAnnotation(Length.class);
                if (str.length() < len.min() || str.length() > len.max()) {
                    violations.add(new Violation(field.getName(), len.message()
                            + " [" + str.length() + " not in " + len.min() + ".." + len.max() + "]"));
                }
            }
        }
        return violations;
    }

    /** Build a SQL CREATE TABLE fragment from @Column annotations. */
    public static String generateDdl(Class<?> cls) {
        StringBuilder sb = new StringBuilder("CREATE TABLE ")
                .append(cls.getSimpleName().toUpperCase()).append(" (\n");
        for (Field field : cls.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Column.class)) continue;
            Column col = field.getAnnotation(Column.class);
            sb.append("  ").append(col.name());
            if (!col.nullable()) sb.append("  NOT NULL");
            sb.append(",\n");
        }
        sb.setLength(sb.length() - 2);
        return sb.append("\n)").toString();
    }
}
