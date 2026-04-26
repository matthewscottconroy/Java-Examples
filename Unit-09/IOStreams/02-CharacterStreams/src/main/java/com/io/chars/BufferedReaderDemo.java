package com.io.chars;

import java.io.*;
import java.nio.file.*;

/**
 * {@link BufferedReader} and {@link BufferedWriter} — the everyday API for text files.
 *
 * <p>{@code BufferedReader.readLine()} is the most commonly used character-stream
 * method: it reads one line at a time, stripping the line terminator,
 * and returns {@code null} at end-of-stream.
 *
 * <p>The decorator stack for character streams mirrors the byte-stream stack:
 * <pre>
 *   FileWriter          ← chars → file (uses platform default charset — avoid!)
 *     └─ BufferedWriter ← adds buffering and newLine()
 *
 *   FileReader           ← file → chars
 *     └─ BufferedReader  ← adds readLine() and buffering
 * </pre>
 *
 * <p><strong>Prefer the NIO.2 Files methods</strong> (example 03) for most
 * file-text operations; they are simpler and always accept an explicit charset.
 * These classes are shown here so you recognise them when reading existing code.
 */
public class BufferedReaderDemo {

    public static void demonstrate(Path file) throws IOException {
        System.out.println("-- BufferedWriter: write lines --");

        // FileWriter(file, charset) was added in Java 11.  Before that,
        // the only safe way was: new OutputStreamWriter(new FileOutputStream(f), charset).
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(file.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {
            writer.write("Line one: the quick brown fox");
            writer.newLine();               // platform-appropriate line separator
            writer.write("Line two: jumps over the lazy dog");
            writer.newLine();
            writer.write("Line three: 日本語テスト");   // non-ASCII to show encoding matters
            writer.newLine();
        }

        System.out.println("-- BufferedReader.readLine() --");
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            int num = 1;
            while ((line = reader.readLine()) != null) {   // null = end of stream
                System.out.println("  [" + num++ + "] " + line);
            }
        }

        System.out.println("-- BufferedReader.lines() — stream API (Java 8+) --");
        try (BufferedReader reader = new BufferedReader(
                new FileReader(file.toFile(), java.nio.charset.StandardCharsets.UTF_8))) {
            reader.lines()
                  .filter(l -> l.contains("fox"))
                  .forEach(l -> System.out.println("  matched: " + l));
        }
    }
}
