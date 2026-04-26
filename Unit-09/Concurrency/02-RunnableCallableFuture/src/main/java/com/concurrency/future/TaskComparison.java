package com.concurrency.future;

import java.util.concurrent.Callable;

/**
 * Contrasts Runnable with Callable.
 *
 * <table border="1" cellpadding="4">
 *   <tr><th></th><th>Runnable</th><th>Callable&lt;V&gt;</th></tr>
 *   <tr><td>Return value</td><td>void</td><td>V</td></tr>
 *   <tr><td>Throws checked</td><td>no</td><td>yes (Exception)</td></tr>
 *   <tr><td>Functional sig</td><td>void run()</td><td>V call() throws Exception</td></tr>
 * </table>
 *
 * <p>Use Runnable when you only need a side effect.
 * Use Callable when you need a result or may encounter a checked exception.
 */
public class TaskComparison {

    // Runnable: fire-and-forget; no return, no checked exception.
    static Runnable logTask(String message) {
        return () -> System.out.println("[Runnable] " + message
                + " on " + Thread.currentThread().getName());
    }

    // Callable<String>: returns a value and may throw.
    static Callable<String> fetchTask(String resource) {
        return () -> {
            System.out.println("[Callable] fetching " + resource
                    + " on " + Thread.currentThread().getName());
            Thread.sleep(30);   // simulated I/O — checked exception is fine here
            if (resource.equals("bad")) throw new Exception("Resource not found: " + resource);
            return "Result of " + resource;
        };
    }
}
