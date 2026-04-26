package com.io.channels;

import java.io.IOException;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * {@link FileChannel} — NIO's replacement for stream-based file I/O.
 *
 * <p>Advantages over {@code FileInputStream}/{@code FileOutputStream}:
 * <ul>
 *   <li>Supports random-access reads and writes at arbitrary positions</li>
 *   <li>{@code transferTo}/{@code transferFrom}: zero-copy file-to-file or
 *       file-to-socket moves that let the OS bypass user space entirely</li>
 *   <li>{@code map()}: memory-mapped files — the file appears as a ByteBuffer
 *       backed directly by OS virtual memory</li>
 *   <li>Integrates with {@code Selector} for non-blocking I/O (sockets only)</li>
 * </ul>
 *
 * <p>For simple sequential reads/writes, the {@link Files} utility class
 * (example 03) is simpler.  Reach for FileChannel when you need random access,
 * large file efficiency, or memory-mapping.
 */
public class FileChannelDemo {

    public static void showReadWrite(Path file) throws IOException {
        System.out.println("-- FileChannel write and read --");

        // Open for writing (creates or truncates the file).
        try (FileChannel ch = FileChannel.open(file,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

            ByteBuffer buf = ByteBuffer.wrap("Hello, FileChannel!\n".getBytes(StandardCharsets.UTF_8));
            int written = ch.write(buf);
            System.out.println("  wrote " + written + " bytes");

            // Random-access write at position 7 (overwrites "FileChannel")
            ByteBuffer patch = ByteBuffer.wrap("NIO2      ".getBytes(StandardCharsets.UTF_8));
            ch.write(patch, 7);
        }

        try (FileChannel ch = FileChannel.open(file, StandardOpenOption.READ)) {
            ByteBuffer buf = ByteBuffer.allocate((int) ch.size());
            ch.read(buf);
            buf.flip();
            System.out.println("  read:  " + StandardCharsets.UTF_8.decode(buf).toString().trim());
        }
    }

    public static void showTransfer(Path src, Path dst) throws IOException {
        System.out.println("\n-- transferTo: efficient file copy --");

        // Prepare source.
        Files.writeString(src, "Data to transfer".repeat(500), StandardCharsets.UTF_8);
        long size = Files.size(src);

        long start = System.nanoTime();
        try (FileChannel from = FileChannel.open(src, StandardOpenOption.READ);
             FileChannel to   = FileChannel.open(dst,
                     StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            // transferTo asks the OS to move bytes directly (sendfile syscall on Linux).
            long transferred = from.transferTo(0, from.size(), to);
            System.out.printf("  transferred %,d bytes in %d µs%n",
                    transferred, (System.nanoTime() - start) / 1000);
        }
        System.out.println("  src size=" + size + "  dst size=" + Files.size(dst));
    }

    public static void showMemoryMapped(Path file) throws IOException {
        System.out.println("\n-- MappedByteBuffer: file as virtual memory --");

        // Write initial content.
        byte[] initial = "Hello, Memory Map!".getBytes(StandardCharsets.UTF_8);
        Files.write(file, initial);

        try (FileChannel ch = FileChannel.open(file,
                StandardOpenOption.READ, StandardOpenOption.WRITE)) {

            // Map the entire file into memory.
            MappedByteBuffer mapped = ch.map(FileChannel.MapMode.READ_WRITE, 0, ch.size());

            System.out.println("  before: " + StandardCharsets.UTF_8.decode(mapped.slice(0, (int) ch.size())));

            // Modifying the mapped buffer modifies the file directly.
            mapped.position(7);
            mapped.put("Mapped File".getBytes(StandardCharsets.UTF_8));
            mapped.force();  // flush OS page cache to storage
        }

        System.out.println("  after:  " + Files.readString(file, StandardCharsets.UTF_8));
    }
}
