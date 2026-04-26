package com.io.bytes;

import java.io.*;
import java.util.Arrays;

/**
 * {@link ByteArrayOutputStream} and {@link ByteArrayInputStream} —
 * in-memory byte streams that use the same interface as file-backed streams.
 *
 * <p>This is the most useful property of the decorator pattern:
 * code written against {@code OutputStream} works identically whether
 * the destination is a file, a network socket, or a byte array in memory.
 *
 * <p>Common uses:
 * <ul>
 *   <li>Unit-testing I/O code without touching the filesystem</li>
 *   <li>Building a byte payload in memory before sending over a network</li>
 *   <li>Implementing a deep-copy (serialize → byte[] → deserialize)</li>
 * </ul>
 */
public class ByteArrayDemo {

    /**
     * Serialize a set of values to a byte array using DataOutputStream,
     * then read them back using DataInputStream — no files involved.
     */
    public static void demonstrate() throws IOException {
        System.out.println("-- Write typed values into a byte[] in memory --");

        ByteArrayOutputStream backing = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(backing)) {
            out.writeInt(2024);
            out.writeDouble(Math.PI);
            out.writeUTF("in-memory");
        }

        byte[] bytes = backing.toByteArray();
        System.out.println("  serialized to " + bytes.length + " bytes");
        System.out.println("  first 4 bytes (big-endian int 2024): "
                + Arrays.toString(Arrays.copyOf(bytes, 4)));

        System.out.println("-- Read back from the same byte[] --");
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            System.out.println("  int:    " + in.readInt());
            System.out.println("  double: " + in.readDouble());
            System.out.println("  UTF:    " + in.readUTF());
        }

        System.out.println("-- PrintStream to ByteArrayOutputStream (captures print output) --");
        ByteArrayOutputStream capture = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(capture);
        ps.println("Captured line one");
        ps.printf("Captured %s%n", "line two");
        ps.flush();
        System.out.println("  captured: " + capture.toString().trim().replace("\n", " | "));
    }
}
