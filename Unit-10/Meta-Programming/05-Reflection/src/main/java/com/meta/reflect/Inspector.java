package com.meta.reflect;

import java.lang.reflect.*;
import java.util.*;

/**
 * Uses the Java Reflection API to inspect classes and invoke methods at runtime.
 *
 * <p>Reflection gives a program the ability to examine and interact with its
 * own structure at runtime — reading field values, listing methods, invoking
 * methods by name, and instantiating classes without knowing their names at
 * compile time.
 *
 * <p>The key entry point is {@link Class} — a runtime descriptor for every
 * Java type. Every object carries a reference to its {@code Class} via
 * {@code getClass()}, and you can obtain one from a type literal with
 * {@code MyClass.class} or from a name string with {@code Class.forName(...)}.
 */
public class Inspector {

    /**
     * Returns a human-readable summary of a class's public API.
     */
    public static String describe(Class<?> cls) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(cls.getName()).append("\n");

        // Superclass and interfaces
        if (cls.getSuperclass() != null && cls.getSuperclass() != Object.class)
            sb.append("  extends: ").append(cls.getSuperclass().getSimpleName()).append("\n");
        if (cls.getInterfaces().length > 0) {
            sb.append("  implements: ");
            for (Class<?> iface : cls.getInterfaces())
                sb.append(iface.getSimpleName()).append(" ");
            sb.append("\n");
        }

        // Fields
        Field[] fields = cls.getDeclaredFields();
        if (fields.length > 0) {
            sb.append("  Fields:\n");
            for (Field f : fields)
                sb.append("    ").append(Modifier.toString(f.getModifiers()))
                  .append(" ").append(f.getType().getSimpleName())
                  .append(" ").append(f.getName()).append("\n");
        }

        // Methods (public only, excluding Object methods)
        Method[] methods = cls.getDeclaredMethods();
        if (methods.length > 0) {
            sb.append("  Methods:\n");
            Arrays.sort(methods, Comparator.comparing(Method::getName));
            for (Method m : methods) {
                if (!Modifier.isPublic(m.getModifiers())) continue;
                sb.append("    ").append(m.getReturnType().getSimpleName())
                  .append(" ").append(m.getName()).append("(");
                StringJoiner params = new StringJoiner(", ");
                for (Class<?> p : m.getParameterTypes()) params.add(p.getSimpleName());
                sb.append(params).append(")\n");
            }
        }
        return sb.toString();
    }

    /**
     * Invokes a public method by name on an object, passing the given arguments.
     *
     * @throws ReflectiveOperationException if the method is not found or throws
     */
    public static Object invoke(Object target, String methodName, Object... args)
            throws ReflectiveOperationException {
        Class<?>[] argTypes = Arrays.stream(args)
            .map(Object::getClass)
            .toArray(Class[]::new);
        Method method = target.getClass().getMethod(methodName, argTypes);
        return method.invoke(target, args);
    }

    /**
     * Reads the value of a field (even a private one) from an object.
     */
    public static Object readField(Object target, String fieldName)
            throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    /**
     * Creates an instance of a class by its fully-qualified name,
     * using the no-arg constructor.
     */
    public static Object instantiate(String className)
            throws ReflectiveOperationException {
        Class<?> cls = Class.forName(className);
        return cls.getDeclaredConstructor().newInstance();
    }

    /**
     * Returns all methods whose name starts with a given prefix.
     * Useful for convention-based discovery (e.g. "get" prefix for getters).
     */
    public static List<Method> findMethodsWithPrefix(Class<?> cls, String prefix) {
        List<Method> result = new ArrayList<>();
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().startsWith(prefix) && Modifier.isPublic(m.getModifiers()))
                result.add(m);
        }
        result.sort(Comparator.comparing(Method::getName));
        return result;
    }
}
