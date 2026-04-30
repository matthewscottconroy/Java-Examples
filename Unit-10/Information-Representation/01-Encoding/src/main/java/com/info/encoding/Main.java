package com.info.encoding;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Encoding — Representing Binary Data as Text ===\n");

        byte[] raw = "Hello, World! 🌍".getBytes(StandardCharsets.UTF_8);

        // ---------------------------------------------------------------
        // 1. Base64
        // ---------------------------------------------------------------
        System.out.println("--- Base64 ---");
        String b64 = Encodings.toBase64(raw);
        byte[] back = Encodings.fromBase64(b64);
        System.out.println("  encoded : " + b64);
        System.out.println("  decoded : " + new String(back, StandardCharsets.UTF_8));
        System.out.println("  url-safe: " + Encodings.toBase64Url(raw));

        // ---------------------------------------------------------------
        // 2. Hex
        // ---------------------------------------------------------------
        System.out.println("\n--- Hex ---");
        String hex = Encodings.toHex(raw);
        System.out.println("  hex    : " + hex);
        System.out.println("  decoded: " + new String(Encodings.fromHex(hex), StandardCharsets.UTF_8));

        // ---------------------------------------------------------------
        // 3. URL encoding
        // ---------------------------------------------------------------
        System.out.println("\n--- URL encoding ---");
        String query = "name=Alice B&score=100%&note=hello world!";
        String encoded = Encodings.urlEncode(query);
        System.out.println("  original: " + query);
        System.out.println("  encoded : " + encoded);
        System.out.println("  decoded : " + Encodings.urlDecode(encoded));

        // ---------------------------------------------------------------
        // 4. Charset widths
        // ---------------------------------------------------------------
        System.out.println("\n--- Charset byte widths ---");
        String sample = "Héllo Wörld 🌍";
        Encodings.encodingWidths(sample)
            .forEach((enc, len) -> System.out.printf("  %-12s %d bytes%n", enc, len));

        // ---------------------------------------------------------------
        // 5. Unicode code points
        // ---------------------------------------------------------------
        System.out.println("\n--- Unicode code points: \"café 🌍\" ---");
        Encodings.codePointDescriptions("café 🌍")
            .forEach(d -> System.out.println("  " + d));

        // ---------------------------------------------------------------
        // 6. Magic byte detection
        // ---------------------------------------------------------------
        System.out.println("\n--- Magic byte detection ---");
        byte[][] headers = {
            { (byte)0x89, 0x50, 0x4E, 0x47 },           // PNG
            { (byte)0xFF, (byte)0xD8, (byte)0xFF },      // JPEG
            { 0x25, 0x50, 0x44, 0x46 },                  // PDF
            { 0x50, 0x4B, 0x03, 0x04 },                  // ZIP
            { 0x1F, (byte)0x8B },                        // GZIP
            { 0x41, 0x42, 0x43 }                         // unknown
        };
        for (byte[] h : headers)
            System.out.printf("  %s  → %s%n", Encodings.toHex(h), Encodings.detectFileType(h));
    }
}
