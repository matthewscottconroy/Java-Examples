package com.io.bytes;

import java.io.*;
import java.nio.file.*;

/**
 * The core design of java.io: the <strong>Decorator pattern</strong>.
 *
 * <p>Every stream class does one thing.  Wrapping one stream in another
 * adds a new capability without changing the wrapped stream's interface.
 * Stacking wrappers builds up the full feature set you need.
 *
 * <pre>
 *   FileOutputStream          ← raw bytes → file
 *     └─ BufferedOutputStream ← adds an in-memory write buffer (fewer syscalls)
 *          └─ DataOutputStream← adds typed-write methods (writeInt, writeUTF…)
 * </pre>
 *
 * <p>Reading mirrors writing exactly — every OutputStream wrapper has a
 * corresponding InputStream wrapper that unwraps the same layers.
 *
 * <p><strong>Key rule:</strong> close only the outermost wrapper.
 * Its {@code close()} cascades inward, flushing and closing every layer.
 */
public class DecoratorDemo {

    public static void demonstrate(Path file) throws IOException {
        System.out.println("-- Writing via the decorator stack --");

        // Try-with-resources closes DataOutputStream, which flushes and closes
        // BufferedOutputStream, which flushes and closes FileOutputStream.
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(file.toFile())))) {

            out.writeInt(42);
            out.writeDouble(3.14159);
            out.writeBoolean(true);
            out.writeUTF("hello, streams");   // length-prefixed modified UTF-8
            System.out.println("  wrote int, double, boolean, UTF string");
        }

        System.out.println("-- Reading via the matching decorator stack --");

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(file.toFile())))) {

            System.out.println("  int:     " + in.readInt());
            System.out.println("  double:  " + in.readDouble());
            System.out.println("  boolean: " + in.readBoolean());
            System.out.println("  UTF:     " + in.readUTF());
        }
    }
}
