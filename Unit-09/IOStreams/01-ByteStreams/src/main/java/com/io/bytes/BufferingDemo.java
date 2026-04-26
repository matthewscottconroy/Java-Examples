package com.io.bytes;

import java.io.*;
import java.nio.file.*;

/**
 * Why buffering matters — timing unbuffered vs. buffered single-byte writes.
 *
 * <p>An unbuffered {@code write(b)} call on a {@link FileOutputStream} makes
 * a system call for every single byte, crossing the user-kernel boundary each
 * time.  A {@link BufferedOutputStream} accumulates bytes in an 8 KB buffer
 * (by default) and only makes one system call when the buffer is full or
 * flushed.  With N bytes that's N system calls vs. ⌈N/8192⌉ system calls.
 *
 * <p>Rule of thumb: <em>always</em> wrap file streams in Buffered* unless
 * you are already writing large arrays at once (in which case the OS kernel
 * does the batching for you).
 */
public class BufferingDemo {

    private static final int BYTES = 200_000;

    static long writeUnbuffered(Path dest) throws IOException {
        long start = System.nanoTime();
        try (OutputStream out = Files.newOutputStream(dest)) {
            for (int i = 0; i < BYTES; i++) {
                out.write(i & 0xFF);    // one byte at a time — one syscall per write
            }
        }
        return System.nanoTime() - start;
    }

    static long writeBuffered(Path dest) throws IOException {
        long start = System.nanoTime();
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(dest))) {
            for (int i = 0; i < BYTES; i++) {
                out.write(i & 0xFF);    // accumulates in 8 KB buffer; far fewer syscalls
            }
        }
        return System.nanoTime() - start;
    }

    public static void demonstrate(Path tempDir) throws IOException {
        System.out.println("-- Buffered vs unbuffered: " + BYTES + " single-byte writes --");

        Path unbufFile = tempDir.resolve("unbuf.bin");
        Path bufFile   = tempDir.resolve("buf.bin");

        // Warm up the JIT and the OS disk cache with one untimed run each.
        writeUnbuffered(unbufFile); writeBuffered(bufFile);

        long unbufMs = writeUnbuffered(unbufFile) / 1_000_000;
        long bufMs   = writeBuffered(bufFile)     / 1_000_000;

        System.out.printf("  Unbuffered: %4d ms%n", unbufMs);
        System.out.printf("  Buffered:   %4d ms%n", bufMs);
        System.out.printf("  Speedup:    ~%.1fx%n",
                unbufMs > 0 ? (double) unbufMs / Math.max(bufMs, 1) : 1.0);

        Files.deleteIfExists(unbufFile);
        Files.deleteIfExists(bufFile);
    }
}
