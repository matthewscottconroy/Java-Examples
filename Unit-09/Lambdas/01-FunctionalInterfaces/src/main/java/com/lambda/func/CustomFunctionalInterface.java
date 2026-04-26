package com.lambda.func;

import java.util.function.Function;

/**
 * Custom functional interfaces and the checked-exception problem.
 *
 * <p>{@code @FunctionalInterface} is optional but recommended:
 * it causes a compile error if you accidentally add a second abstract method.
 *
 * <p><strong>Checked exceptions and lambdas:</strong>
 * {@code Function<T,R>} declares {@code apply} with no checked exceptions,
 * so a lambda that calls a method throwing a checked exception won't compile.
 * Two common solutions:
 * <ol>
 *   <li>Define a custom functional interface that declares {@code throws Exception}.</li>
 *   <li>Write a static wrapper that catches and re-throws as unchecked.</li>
 * </ol>
 */
public class CustomFunctionalInterface {

    // -----------------------------------------------------------------------
    // Custom interface: a transformer that may throw a checked exception
    // -----------------------------------------------------------------------
    @FunctionalInterface
    interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    // -----------------------------------------------------------------------
    // Wrapper: lifts a CheckedFunction into a regular Function
    // -----------------------------------------------------------------------
    static <T, R> Function<T, R> wrap(CheckedFunction<T, R> fn) {
        return t -> {
            try {
                return fn.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    // -----------------------------------------------------------------------
    // Custom interface representing a thunk (deferred computation)
    // -----------------------------------------------------------------------
    @FunctionalInterface
    interface Thunk<T> {
        T evaluate();   // like Supplier<T> but semantically "lazy evaluation"

        // Default methods are allowed — they don't make it non-functional.
        default Thunk<T> memoized() {
            Object[] cache = {null};
            boolean[] computed = {false};
            return () -> {
                if (!computed[0]) { cache[0] = evaluate(); computed[0] = true; }
                @SuppressWarnings("unchecked") T result = (T) cache[0];
                return result;
            };
        }
    }

    static String simulatedIO(String path) throws Exception {
        // Throws a checked exception — can't use inside Function<> directly.
        if (path.equals("bad")) throw new Exception("File not found: " + path);
        return "content of " + path;
    }

    public static void demonstrate() {
        System.out.println("-- Custom @FunctionalInterface --");
        CheckedFunction<String, String> reader = CustomFunctionalInterface::simulatedIO;
        try {
            System.out.println("  good path: " + reader.apply("config.txt"));
            System.out.println("  bad path:  " + reader.apply("bad"));
        } catch (Exception e) {
            System.out.println("  caught: " + e.getMessage());
        }

        System.out.println("\n-- wrap(): lift CheckedFunction into Function --");
        Function<String, String> safeReader = wrap(CustomFunctionalInterface::simulatedIO);
        System.out.println("  good path: " + safeReader.apply("data.csv"));
        try {
            safeReader.apply("bad");
        } catch (RuntimeException e) {
            System.out.println("  wrapped exception: " + e.getCause().getMessage());
        }

        System.out.println("\n-- Thunk (lazy evaluation) --");
        int[] computeCount = {0};
        Thunk<String> expensive = () -> {
            computeCount[0]++;
            System.out.println("  [computing...]");
            return "expensive result";
        };

        Thunk<String> memoized = expensive.memoized();
        System.out.println("  First call:  " + memoized.evaluate());
        System.out.println("  Second call: " + memoized.evaluate());   // no recomputation
        System.out.println("  Compute count: " + computeCount[0]);
    }
}
