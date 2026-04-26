package com.io.channels;

import java.nio.file.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Path tempDir = Files.createTempDirectory("io-channels-");
        Path file1   = tempDir.resolve("channel.bin");
        Path file2   = tempDir.resolve("transfer-src.txt");
        Path file3   = tempDir.resolve("transfer-dst.txt");
        Path file4   = tempDir.resolve("mapped.txt");

        try {
            System.out.println("=== ByteBuffer mechanics ===");
            ByteBufferDemo.demonstrate();

            System.out.println("\n=== FileChannel ===");
            FileChannelDemo.showReadWrite(file1);
            FileChannelDemo.showTransfer(file2, file3);
            FileChannelDemo.showMemoryMapped(file4);

            System.out.println("\n=== When to use what ===");
            System.out.println("  Files.readString / writeString   → simple text files (≤ a few MB)");
            System.out.println("  BufferedInputStream/OutputStream → sequential byte streaming");
            System.out.println("  FileChannel.read/write           → large files, random access");
            System.out.println("  FileChannel.transferTo           → file copy / file-to-socket");
            System.out.println("  MappedByteBuffer                 → in-place editing, huge files");
        } finally {
            for (Path p : new Path[]{file1, file2, file3, file4}) Files.deleteIfExists(p);
            Files.deleteIfExists(tempDir);
        }
    }
}
