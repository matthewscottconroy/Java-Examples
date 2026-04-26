package com.generics.patterns;

import java.util.function.UnaryOperator;

/**
 * Generic singleton factory — one shared instance, any type parameter.
 *
 * <p>A generic singleton stores no state related to T, so a single instance
 * is safe to share regardless of what T is resolved to.  The trick is an
 * unchecked cast from the raw (erased) instance to {@code Singleton<T>}.
 *
 * <p>This is exactly how {@code Collections.emptyList()},
 * {@code Collections.emptySet()}, and {@code Collections.emptyMap()} work
 * in the standard library.
 */
public class GenericSingleton {

    // -----------------------------------------------------------------------
    // 1. Identity function singleton
    //    UnaryOperator<T> that simply returns its argument — no state needed.
    // -----------------------------------------------------------------------
    @SuppressWarnings("rawtypes")
    private static final UnaryOperator IDENTITY_OP = t -> t;

    // The cast is safe: IDENTITY_OP has no fields and never inspects T.
    @SuppressWarnings("unchecked")
    public static <T> UnaryOperator<T> identityOperator() {
        return (UnaryOperator<T>) IDENTITY_OP;
    }

    // -----------------------------------------------------------------------
    // 2. Immutable empty wrapper singleton
    // -----------------------------------------------------------------------
    public static final class EmptyWrapper<T> {

        @SuppressWarnings("rawtypes")
        private static final EmptyWrapper<?> INSTANCE = new EmptyWrapper<>();

        private EmptyWrapper() {}

        @SuppressWarnings("unchecked")
        public static <T> EmptyWrapper<T> getInstance() {
            return (EmptyWrapper<T>) INSTANCE;
        }

        public boolean isEmpty()  { return true; }
        public int     size()     { return 0; }

        @Override public String toString() { return "EmptyWrapper[]"; }
    }

    // -----------------------------------------------------------------------
    // 3. Memoizing factory — creates at most one instance per Class<T> key.
    //    Combines the type token pattern (example 05) with a singleton cache.
    // -----------------------------------------------------------------------
    private static final java.util.Map<Class<?>, Object> cache = new java.util.HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getOrCreate(Class<T> clazz) throws ReflectiveOperationException {
        if (!cache.containsKey(clazz)) {
            cache.put(clazz, clazz.getDeclaredConstructor().newInstance());
        }
        return (T) cache.get(clazz);
    }
}
