package com.io.chars;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HexFormat;

/**
 * {@link InputStreamReader} and {@link OutputStreamWriter} — the byte↔character bridge.
 *
 * <p>A character stream is a byte stream plus a {@link Charset} that
 * converts between the two.  {@code InputStreamReader} wraps any
 * {@code InputStream} and decodes bytes to chars using a specified charset.
 * {@code OutputStreamWriter} does the reverse.
 *
 * <p><strong>Always specify the charset explicitly.</strong>
 * The no-arg constructors of {@code FileReader} / {@code FileWriter} use the
 * platform default charset, which differs between operating systems.  A file
 * written on Windows (often Cp1252) may be read incorrectly on Linux (UTF-8).
 *
 * <p>Standard charsets in {@link StandardCharsets} are guaranteed to exist
 * on every JVM: UTF_8, UTF_16, US_ASCII, ISO_8859_1.
 */
public class BridgeAndEncodingDemo {

    public static void showBridge() throws IOException {
        System.out.println("-- InputStreamReader wrapping System.in (concept) --");
        // In production: new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8))
        // Here we simulate with a byte array carrying UTF-8 bytes.
        String original = "Héllo wörld";
        byte[] utf8Bytes = original.getBytes(StandardCharsets.UTF_8);

        ByteArrayInputStream byteSource = new ByteArrayInputStream(utf8Bytes);
        // Wrap the byte stream with a charset decoder:
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(byteSource, StandardCharsets.UTF_8))) {
            System.out.println("  decoded: " + reader.readLine());
        }
    }

    public static void showEncodingDifference(Path tempDir) throws IOException {
        System.out.println("\n-- Same text, different encodings on disk --");
        String text = "Café résumé naïve";

        Path utf8File   = tempDir.resolve("utf8.txt");
        Path latin1File = tempDir.resolve("latin1.txt");

        Files.writeString(utf8File,   text, StandardCharsets.UTF_8);
        Files.writeString(latin1File, text, StandardCharsets.ISO_8859_1);

        System.out.println("  text: " + text);
        System.out.println("  UTF-8   file size: " + Files.size(utf8File)   + " bytes");
        System.out.println("  Latin-1 file size: " + Files.size(latin1File) + " bytes");

        // Show the on-disk bytes differ (UTF-8 uses 2 bytes for é, à, etc.)
        byte[] utf8raw   = Files.readAllBytes(utf8File);
        byte[] latin1raw = Files.readAllBytes(latin1File);
        HexFormat hex = HexFormat.of().withUpperCase();
        System.out.println("  UTF-8   hex:   " + hex.formatHex(utf8raw));
        System.out.println("  Latin-1 hex:   " + hex.formatHex(latin1raw));

        System.out.println("\n-- Reading with the WRONG encoding --");
        String misread = Files.readString(utf8File, StandardCharsets.ISO_8859_1);
        System.out.println("  UTF-8 file read as Latin-1: " + misread + "  ← mojibake");
        String correct = Files.readString(utf8File, StandardCharsets.UTF_8);
        System.out.println("  UTF-8 file read as UTF-8:   " + correct  + "  ← correct");

        Files.deleteIfExists(utf8File);
        Files.deleteIfExists(latin1File);
    }

    public static void showPrintWriter() throws IOException {
        System.out.println("\n-- PrintWriter: convenience formatting to any Writer --");
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            pw.printf("Name: %-10s Age: %3d%n", "Alice", 30);
            pw.printf("Name: %-10s Age: %3d%n", "Bob",   25);
        }
        System.out.println(sw.toString().trim().replace("\n", "\n  ").indent(2).stripTrailing());
    }
}
