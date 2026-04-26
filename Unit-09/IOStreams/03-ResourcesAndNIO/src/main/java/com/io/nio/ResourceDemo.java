package com.io.nio;

import java.io.*;

/**
 * Safe resource management with try-with-resources.
 *
 * <p>Before Java 7, closing streams required a {@code finally} block with a
 * nested try/catch, and an exception thrown in {@code close()} would silently
 * <em>replace</em> the original exception from the body.
 *
 * <p>Try-with-resources solves both problems:
 * <ol>
 *   <li>Resources are <em>always</em> closed in the reverse order they were opened,
 *       even if the body throws.</li>
 *   <li>If both body and close() throw, the close exception is attached as a
 *       <em>suppressed</em> exception — the original exception is not lost.</li>
 * </ol>
 *
 * <p>Any class implementing {@link AutoCloseable} qualifies.
 */
public class ResourceDemo {

    // -----------------------------------------------------------------------
    // Multiple resources in one try-with-resources — closed in reverse order
    // -----------------------------------------------------------------------
    public static void showMultipleResources() throws IOException {
        System.out.println("-- Multiple resources closed in reverse order --");

        byte[] payload = "hello world".getBytes();

        // Resources are listed left-to-right; closed right-to-left (LIFO).
        // In this case: DataOutputStream → BufferedOutputStream → ByteArrayOutputStream
        ByteArrayOutputStream backing = new ByteArrayOutputStream();
        try (BufferedOutputStream buf = new BufferedOutputStream(backing);
             DataOutputStream     dat = new DataOutputStream(buf)) {
            dat.writeUTF("hello");
            dat.writeInt(42);
            System.out.println("  wrote " + dat.size() + " bytes through the stack");
        }
        System.out.println("  backing buffer size after close: " + backing.size());
    }

    // -----------------------------------------------------------------------
    // Custom AutoCloseable — anything can participate in try-with-resources
    // -----------------------------------------------------------------------
    static class TrackedResource implements AutoCloseable {
        private final String name;
        TrackedResource(String name) {
            this.name = name;
            System.out.println("  open:  " + name);
        }
        @Override public void close() {
            System.out.println("  close: " + name);
        }
    }

    public static void showCustomAutoCloseable() {
        System.out.println("\n-- Custom AutoCloseable: LIFO close order --");
        try (TrackedResource a = new TrackedResource("A");
             TrackedResource b = new TrackedResource("B");
             TrackedResource c = new TrackedResource("C")) {
            System.out.println("  (inside try block)");
        }
        // Expected close order: C, B, A
    }

    // -----------------------------------------------------------------------
    // Suppressed exceptions — the original exception is preserved
    // -----------------------------------------------------------------------
    static class FailOnClose implements AutoCloseable {
        @Override public void close() throws Exception {
            throw new Exception("close() failed");
        }
    }

    public static void showSuppressedException() {
        System.out.println("\n-- Suppressed exception: body exception wins --");
        try (FailOnClose r = new FailOnClose()) {
            throw new RuntimeException("body failed");
        } catch (RuntimeException e) {
            System.out.println("  caught: " + e.getMessage());
            for (Throwable sup : e.getSuppressed()) {
                System.out.println("  suppressed: " + sup.getMessage());
            }
        } catch (Exception ignored) {}
    }
}
