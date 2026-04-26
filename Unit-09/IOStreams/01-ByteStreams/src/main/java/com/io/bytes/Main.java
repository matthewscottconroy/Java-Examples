package com.io.bytes;

import java.nio.file.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Path tempDir  = Files.createTempDirectory("io-bytes-");
        Path tempFile = tempDir.resolve("data.bin");

        try {
            System.out.println("=== Decorator Pattern: DataOutputStream over BufferedOutputStream over FileOutputStream ===");
            DecoratorDemo.demonstrate(tempFile);

            System.out.println("\n=== ByteArrayOutputStream — in-memory stream, same interface ===");
            ByteArrayDemo.demonstrate();

            System.out.println("\n=== Buffering performance ===");
            BufferingDemo.demonstrate(tempDir);

            // Show the raw bytes of a simple file write for intuition.
            System.out.println("\n=== What raw FileInputStream.read() looks like ===");
            Files.write(tempFile, new byte[]{72, 101, 108, 108, 111}); // "Hello" in ASCII
            try (var in = Files.newInputStream(tempFile)) {
                int b;
                System.out.print("  bytes: ");
                while ((b = in.read()) != -1) {   // -1 signals end of stream
                    System.out.printf("%3d", b);
                }
                System.out.println();
            }
            System.out.println("  (72=H 101=e 108=l 108=l 111=o)");
        } finally {
            Files.deleteIfExists(tempFile);
            Files.deleteIfExists(tempDir);
        }
    }
}
