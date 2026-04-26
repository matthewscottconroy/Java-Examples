package com.generics.erasure;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Recovering type information at runtime using "type tokens."
 *
 * <p>Because instance type arguments are erased, code that needs to know T
 * at runtime must receive that information explicitly.  The idiomatic Java
 * solution is to pass a {@code Class<T>} object — called a type token.
 *
 * <p>For parameterized types like {@code List<String>} (where
 * {@code Class<List<String>>} doesn't exist), the super-type token trick
 * captures the full generic type in the bytecode-level class metadata of an
 * anonymous subclass (which survives erasure).
 */
public class TypeToken {

    // -----------------------------------------------------------------------
    // 1. Simple type token: Class<T>
    //    Works for non-parameterized types (String, Integer, MyClass, …).
    // -----------------------------------------------------------------------

    /**
     * Creates an instance of T using its no-arg constructor.
     * The Class<T> token tells us which constructor to call at runtime.
     */
    public static <T> T newInstance(Class<T> clazz) throws ReflectiveOperationException {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * A typesafe heterogeneous container (Effective Java item 33).
     *
     * <p>Stores values of arbitrary, unrelated types under their own Class key.
     * Because put() ties Class<T> to T, get() can return T without an unsafe cast
     * visible to callers — the only unchecked cast is hidden here behind Class.cast().
     */
    public static class TypedMap {
        private final Map<Class<?>, Object> map = new HashMap<>();

        public <T> void put(Class<T> type, T value) {
            map.put(type, value);
        }

        // Class.cast() performs the checked cast using reflection — safe because
        // put() enforced the Class<T>↔value-of-type-T invariant.
        public <T> T get(Class<T> type) {
            return type.cast(map.get(type));
        }

        public boolean contains(Class<?> type) {
            return map.containsKey(type);
        }
    }

    // -----------------------------------------------------------------------
    // 2. Super-type token: captures parameterized types like List<String>.
    //
    //    Trick: subclassing an abstract class pinning a generic parameter
    //    leaves the concrete type in the bytecode as the "generic superclass."
    //    That metadata survives erasure and is readable via reflection.
    //
    //    Usage:
    //      SuperTypeToken<List<String>> token = new SuperTypeToken<List<String>>() {};
    //      token.getType()  →  java.util.List<java.lang.String>
    //
    //    This pattern is the foundation of Guava's TypeToken and Jackson's
    //    TypeReference<T>.
    // -----------------------------------------------------------------------
    public abstract static class SuperTypeToken<T> {

        private final Type type;

        protected SuperTypeToken() {
            // The anonymous subclass's "generic superclass" is SuperTypeToken<T>
            // with T filled in — that information IS preserved in the class file.
            Type superclass = getClass().getGenericSuperclass();
            if (!(superclass instanceof ParameterizedType pt)) {
                throw new IllegalStateException("Must be created as an anonymous subclass");
            }
            this.type = pt.getActualTypeArguments()[0];
        }

        public Type getType() { return type; }

        @Override
        public String toString() { return "SuperTypeToken<" + type.getTypeName() + ">"; }
    }
}
