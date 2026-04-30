package com.meta.proxy;

import java.lang.reflect.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;

/**
 * Factory for dynamic proxy decorators that add cross-cutting behaviour
 * to any interface without modifying the implementation class.
 *
 * <p>{@link java.lang.reflect.Proxy} creates an object at runtime that
 * implements a given interface. Every method call on that object is
 * intercepted by an {@link InvocationHandler}, which can add behaviour
 * before and after delegating to the real implementation.
 *
 * <p>This is the runtime equivalent of the Decorator design pattern,
 * but without writing a separate class per combination of concerns.
 * The same handler logic wraps any interface uniformly.
 */
public class ServiceProxy {

    /**
     * Returns a proxy that logs every method call (name, args, return value,
     * and any exception) to a {@link List<String>} log collector.
     */
    @SuppressWarnings("unchecked")
    public static <T> T logging(T target, Class<T> iface, List<String> log) {
        return (T) Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class[]{ iface },
            (proxy, method, args) -> {
                String call = method.getName() + "(" + formatArgs(args) + ")";
                try {
                    Object result = method.invoke(target, args);
                    log.add("CALL  " + call + " → " + result);
                    return result;
                } catch (InvocationTargetException e) {
                    log.add("ERROR " + call + " threw " + e.getCause().getClass().getSimpleName());
                    throw e.getCause();
                }
            }
        );
    }

    /**
     * Returns a proxy that measures and records the wall-clock time of
     * every method call in a {@link Map} from method name to nanoseconds.
     */
    @SuppressWarnings("unchecked")
    public static <T> T timing(T target, Class<T> iface, Map<String, Long> timings) {
        return (T) Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class[]{ iface },
            (proxy, method, args) -> {
                Instant start = Instant.now();
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                } finally {
                    long ns = Duration.between(start, Instant.now()).toNanos();
                    timings.merge(method.getName(), ns, Long::sum);
                }
            }
        );
    }

    /**
     * Returns a proxy that caches return values keyed by method name + args.
     * On a cache hit the real method is not called.
     */
    @SuppressWarnings("unchecked")
    public static <T> T caching(T target, Class<T> iface) {
        Map<String, Object> cache = new HashMap<>();
        return (T) Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class[]{ iface },
            (proxy, method, args) -> {
                String key = method.getName() + Arrays.toString(args);
                if (cache.containsKey(key)) return cache.get(key);
                try {
                    Object result = method.invoke(target, args);
                    cache.put(key, result);
                    return result;
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
        );
    }

    /**
     * Returns a proxy that retries a method call up to {@code maxAttempts}
     * times when it throws a {@link RuntimeException}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T retrying(T target, Class<T> iface, int maxAttempts) {
        return (T) Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class[]{ iface },
            (proxy, method, args) -> {
                int attempt = 0;
                while (true) {
                    try {
                        return method.invoke(target, args);
                    } catch (InvocationTargetException e) {
                        attempt++;
                        if (!(e.getCause() instanceof RuntimeException) || attempt >= maxAttempts)
                            throw e.getCause();
                    }
                }
            }
        );
    }

    // ---------------------------------------------------------------

    private static String formatArgs(Object[] args) {
        if (args == null) return "";
        StringJoiner sj = new StringJoiner(", ");
        for (Object a : args) sj.add(String.valueOf(a));
        return sj.toString();
    }
}
