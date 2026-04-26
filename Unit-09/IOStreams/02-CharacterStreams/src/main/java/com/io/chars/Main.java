package com.io.chars;

import java.io.*;
import java.nio.file.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Path tempDir  = Files.createTempDirectory("io-chars-");
        Path textFile = tempDir.resolve("sample.txt");

        try {
            System.out.println("=== BufferedReader / BufferedWriter ===");
            BufferedReaderDemo.demonstrate(textFile);

            System.out.println("\n=== InputStreamReader bridge and charset encoding ===");
            BridgeAndEncodingDemo.showBridge();
            BridgeAndEncodingDemo.showEncodingDifference(tempDir);
            BridgeAndEncodingDemo.showPrintWriter();

            // Demonstrate wrapping System.in — the canonical real-world bridge usage.
            System.out.println("\n=== System.in is a byte stream; wrap it for text ---");
            System.out.println("  System.in  type: " + System.in.getClass().getSimpleName());
            System.out.println("  System.out type: " + System.out.getClass().getSimpleName());
            System.out.println("  System.err type: " + System.err.getClass().getSimpleName());
            System.out.println("  To read text from stdin:");
            System.out.println("    BufferedReader stdin =");
            System.out.println("      new BufferedReader(new InputStreamReader(System.in, UTF_8));");
        } finally {
            Files.deleteIfExists(textFile);
            Files.deleteIfExists(tempDir);
        }
    }
}
