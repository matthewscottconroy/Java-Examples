package com.meta.annotations;

import java.lang.reflect.Field;

/**
 * Reflectively inspects an object's fields for constraint annotations
 * ({@link Required}, {@link Range}, {@link Pattern}) and collects any
 * violations into a {@link ValidationResult}.
 *
 * <p>Only declared fields on the object's own class are checked (no
 * inherited fields). Fields are made accessible so private fields work too.
 */
public final class Validator {

    private Validator() {}

    public static ValidationResult validate(Object obj) {
        ValidationResult result = new ValidationResult();
        Class<?> cls = obj.getClass();

        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(obj);
            } catch (IllegalAccessException e) {
                continue;
            }

            checkRequired(field, value, result);
            checkRange(field, value, result);
            checkPattern(field, value, result);
        }
        return result;
    }

    private static void checkRequired(Field field, Object value, ValidationResult result) {
        Required ann = field.getAnnotation(Required.class);
        if (ann == null) return;
        if (value == null || (value instanceof String s && s.isBlank())) {
            result.addViolation(field.getName(), ann.message());
        }
    }

    private static void checkRange(Field field, Object value, ValidationResult result) {
        Range ann = field.getAnnotation(Range.class);
        if (ann == null || value == null) return;
        double d;
        if (value instanceof Number n) d = n.doubleValue();
        else return;
        if (d < ann.min() || d > ann.max()) {
            result.addViolation(field.getName(), ann.message()
                + " [" + ann.min() + ", " + ann.max() + "], got " + d);
        }
    }

    private static void checkPattern(Field field, Object value, ValidationResult result) {
        Pattern ann = field.getAnnotation(Pattern.class);
        if (ann == null || value == null) return;
        if (!(value instanceof String s)) return;
        if (!s.matches(ann.regex())) {
            result.addViolation(field.getName(), ann.message() + " /" + ann.regex() + "/");
        }
    }
}
