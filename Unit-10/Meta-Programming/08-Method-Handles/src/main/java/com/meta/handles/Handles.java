package com.meta.handles;

import java.lang.invoke.*;
import java.util.function.*;

/**
 * Demonstrates {@link MethodHandle} — a typed, directly-executable reference
 * to any method, constructor, or field, resolved once and invoked many times.
 *
 * <p>MethodHandles sit between reflection (flexible but slow) and direct
 * bytecode (fast but rigid). The JVM can inline and optimise handles that
 * are held in {@code static final} fields.
 *
 * <p>Key combinators shown here:
 * <ul>
 *   <li>{@link MethodHandles#filterArguments} — pre-process arguments
 *   <li>{@link MethodHandles#foldArguments} — prepend a derived argument
 *   <li>{@link MethodHandles#filterReturnValue} — post-process return value
 *   <li>{@link MethodHandle#bindTo} — partial application / currying
 *   <li>{@link MethodHandles#guardWithTest} — inline branch based on a predicate
 * </ul>
 */
public final class Handles {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private Handles() {}

    // ---------------------------------------------------------------
    // 1. Basic virtual method handle
    // ---------------------------------------------------------------

    /** Returns a handle to {@code String.toUpperCase()}. */
    public static MethodHandle upperCaseHandle() throws NoSuchMethodException, IllegalAccessException {
        return LOOKUP.findVirtual(String.class, "toUpperCase", MethodType.methodType(String.class));
    }

    /** Returns a handle to {@code String.substring(int, int)}. */
    public static MethodHandle substringHandle() throws NoSuchMethodException, IllegalAccessException {
        return LOOKUP.findVirtual(String.class, "substring",
            MethodType.methodType(String.class, int.class, int.class));
    }

    // ---------------------------------------------------------------
    // 2. Static method handle
    // ---------------------------------------------------------------

    /** Returns a handle to {@code Integer.parseInt(String)}. */
    public static MethodHandle parseIntHandle() throws NoSuchMethodException, IllegalAccessException {
        return LOOKUP.findStatic(Integer.class, "parseInt",
            MethodType.methodType(int.class, String.class));
    }

    // ---------------------------------------------------------------
    // 3. Constructor handle
    // ---------------------------------------------------------------

    /** Returns a handle to {@code StringBuilder(String)}. */
    public static MethodHandle stringBuilderHandle() throws NoSuchMethodException, IllegalAccessException {
        return LOOKUP.findConstructor(StringBuilder.class,
            MethodType.methodType(void.class, String.class));
    }

    // ---------------------------------------------------------------
    // 4. filterArguments — pre-process each argument
    // ---------------------------------------------------------------

    /**
     * Builds a handle for {@code String.concat(String)} that trims both
     * receiver and argument before concatenating.
     *
     * <p>Signature of the returned handle:
     * {@code (String receiver, String arg) -> receiver.trim().concat(arg.trim())}
     */
    public static MethodHandle trimBeforeConcat() throws NoSuchMethodException, IllegalAccessException {
        MethodHandle concat = LOOKUP.findVirtual(String.class, "concat",
            MethodType.methodType(String.class, String.class));
        MethodHandle trim = LOOKUP.findVirtual(String.class, "trim",
            MethodType.methodType(String.class));
        // filterArguments applies a filter to each positional argument.
        // Position 0 is the receiver (String), position 1 is the String arg.
        return MethodHandles.filterArguments(concat, 0, trim, trim);
    }

    // ---------------------------------------------------------------
    // 5. filterReturnValue — post-process the result
    // ---------------------------------------------------------------

    /**
     * Returns a handle that uppercases the result of calling
     * {@code String.concat(String)}.
     */
    public static MethodHandle concatThenUpper() throws NoSuchMethodException, IllegalAccessException {
        MethodHandle concat = LOOKUP.findVirtual(String.class, "concat",
            MethodType.methodType(String.class, String.class));
        MethodHandle upper = LOOKUP.findVirtual(String.class, "toUpperCase",
            MethodType.methodType(String.class));
        return MethodHandles.filterReturnValue(concat, upper);
    }

    // ---------------------------------------------------------------
    // 6. bindTo — partial application
    // ---------------------------------------------------------------

    /**
     * Returns a handle with the receiver bound to {@code prefix}, so the
     * resulting handle is effectively {@code (String arg) -> prefix.concat(arg)}.
     */
    public static MethodHandle prefixWith(String prefix) throws NoSuchMethodException, IllegalAccessException {
        MethodHandle concat = LOOKUP.findVirtual(String.class, "concat",
            MethodType.methodType(String.class, String.class));
        return concat.bindTo(prefix);
    }

    // ---------------------------------------------------------------
    // 7. guardWithTest — branch on a predicate
    // ---------------------------------------------------------------

    /**
     * Returns a handle that calls {@code ifTrue} when the argument is longer
     * than 5 characters, and {@code ifFalse} otherwise.
     *
     * <p>Both branches must share the same method type:
     * {@code (String) -> String}.
     */
    public static MethodHandle longOrShortLabel() throws NoSuchMethodException, IllegalAccessException {
        MethodHandle upper = LOOKUP.findVirtual(String.class, "toUpperCase",
            MethodType.methodType(String.class));
        MethodHandle lower = LOOKUP.findVirtual(String.class, "toLowerCase",
            MethodType.methodType(String.class));
        MethodHandle isLong = LOOKUP.findStatic(Handles.class, "isLong",
            MethodType.methodType(boolean.class, String.class));
        return MethodHandles.guardWithTest(isLong, upper, lower);
    }

    /** Predicate used by {@link #longOrShortLabel()}. */
    static boolean isLong(String s) { return s.length() > 5; }

    // ---------------------------------------------------------------
    // 8. Convert MethodHandle to a functional interface via invokeExact
    // ---------------------------------------------------------------

    /**
     * Wraps a {@code (String) -> String} handle in a {@link UnaryOperator} so
     * it can be used with standard functional APIs.
     */
    public static UnaryOperator<String> asOperator(MethodHandle mh) {
        return s -> {
            try {
                return (String) mh.invoke(s);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        };
    }
}
