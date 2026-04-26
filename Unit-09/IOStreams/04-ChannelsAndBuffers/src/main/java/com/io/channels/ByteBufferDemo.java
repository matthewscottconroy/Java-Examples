package com.io.channels;

import java.nio.ByteBuffer;

/**
 * {@link ByteBuffer} — the central data container in NIO.
 *
 * <p>A ByteBuffer has three state variables that control what is readable
 * and writable at any moment:
 *
 * <pre>
 *   capacity  — fixed; total number of bytes in the buffer
 *   limit     — index of the first byte that should NOT be read or written
 *   position  — index of the next byte to read or write
 *
 *   Invariant: 0 ≤ position ≤ limit ≤ capacity
 * </pre>
 *
 * <p>The two most important state transitions:
 * <ul>
 *   <li>{@code flip()}  — after writing: sets limit=position, position=0.
 *                         Switches the buffer from "write mode" to "read mode".</li>
 *   <li>{@code clear()} — after reading: sets position=0, limit=capacity.
 *                         Switches the buffer back to "write mode".</li>
 *   <li>{@code rewind()} — re-read from the beginning: position=0, limit unchanged.</li>
 *   <li>{@code compact()} — discard already-read data, keep unread data for re-reading later.</li>
 * </ul>
 */
public class ByteBufferDemo {

    public static void demonstrate() {
        System.out.println("-- ByteBuffer state lifecycle --");

        ByteBuffer buf = ByteBuffer.allocate(10);  // heap buffer, capacity=10
        System.out.printf("  initial:  pos=%d  lim=%d  cap=%d%n",
                buf.position(), buf.limit(), buf.capacity());

        // --- Write phase ---
        buf.put((byte) 'H');
        buf.put((byte) 'e');
        buf.put((byte) 'l');
        buf.put((byte) 'l');
        buf.put((byte) 'o');
        System.out.printf("  after 5 puts: pos=%d  lim=%d%n",
                buf.position(), buf.limit());

        // --- flip(): switch to read mode ---
        buf.flip();
        System.out.printf("  after flip(): pos=%d  lim=%d  (ready to read 5 bytes)%n",
                buf.position(), buf.limit());

        // --- Read phase ---
        byte[] dst = new byte[buf.remaining()];
        buf.get(dst);
        System.out.println("  read: " + new String(dst));
        System.out.printf("  after get:    pos=%d  lim=%d%n",
                buf.position(), buf.limit());

        // --- clear(): reset for another write ---
        buf.clear();
        System.out.printf("  after clear(): pos=%d  lim=%d  cap=%d%n",
                buf.position(), buf.limit(), buf.capacity());

        System.out.println("\n-- Typed put/get methods --");
        ByteBuffer typed = ByteBuffer.allocate(32);
        typed.putInt(Integer.MAX_VALUE);
        typed.putDouble(Math.PI);
        typed.putShort((short) 42);
        System.out.printf("  wrote int+double+short, position=%d%n", typed.position());

        typed.flip();
        System.out.println("  int:    " + typed.getInt());
        System.out.println("  double: " + typed.getDouble());
        System.out.println("  short:  " + typed.getShort());

        System.out.println("\n-- Direct vs heap buffer --");
        ByteBuffer heap   = ByteBuffer.allocate(1024);       // lives in JVM heap
        ByteBuffer direct = ByteBuffer.allocateDirect(1024); // lives outside JVM heap (OS-managed)
        System.out.println("  heap.isDirect():   " + heap.isDirect());
        System.out.println("  direct.isDirect(): " + direct.isDirect());
        System.out.println("  Direct buffers avoid a copy when used with FileChannel/SocketChannel.");
    }
}
