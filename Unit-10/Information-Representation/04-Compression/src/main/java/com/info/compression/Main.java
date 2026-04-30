package com.info.compression;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("=== Compression — Removing Redundancy from Data ===\n");

        // ---------------------------------------------------------------
        // 1. Run-length encoding
        // ---------------------------------------------------------------
        System.out.println("--- Run-length encoding (RLE) ---");
        String[] rleInputs = {
            "AAABBBBBCCDDDDDDDD",
            "ABCDEFGH",             // no repetition — expands
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" // 52 A's
        };
        for (String s : rleInputs) {
            String encoded = RunLengthEncoder.encode(s);
            String decoded = RunLengthEncoder.decode(encoded);
            System.out.printf("  %-54s → %s  (saved %+d chars)%n",
                "\"" + (s.length() > 20 ? s.substring(0, 20) + "…" : s) + "\"",
                encoded,
                s.length() - encoded.length());
        }

        // ---------------------------------------------------------------
        // 2. Huffman coding
        // ---------------------------------------------------------------
        System.out.println("\n--- Huffman coding ---");

        String[] huffInputs = {
            "abracadabra",
            "aaaaaaaabbbbccdd",
            "the quick brown fox jumps over the lazy dog"
        };

        for (String text : huffInputs) {
            HuffmanCoder.CompressedData cd = HuffmanCoder.compress(text);
            String recovered = HuffmanCoder.decompress(cd);
            System.out.printf("  Input   : \"%s\"%n", text.length() > 40 ? text.substring(0,40)+"…" : text);
            System.out.printf("  Codes   : %s%n", cd.codeTable());
            System.out.printf("  Bits    : %d (original %d × 8 = %d)%n",
                cd.bits().length(), text.length(), text.length() * 8);
            System.out.printf("  Ratio   : %.2f:1   Lossless: %b%n%n",
                cd.compressionRatio(), text.equals(recovered));
        }

        // ---------------------------------------------------------------
        // 3. GZIP (general-purpose LZ77 + Huffman)
        // ---------------------------------------------------------------
        System.out.println("--- GZIP comparison ---");
        String[] gzipInputs = {
            "hello world",
            "a".repeat(1000),
            "the quick brown fox".repeat(50)
        };
        for (String s : gzipInputs) {
            int gz = gzipSize(s);
            System.out.printf("  %-30s  original=%5d  gzip=%4d  ratio=%.2f%n",
                "\"" + (s.length() > 20 ? s.substring(0, 20) + "…\"" : s + "\""),
                s.length(), gz, (double) s.length() / gz);
        }
    }

    private static int gzipSize(String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(baos)) {
            gz.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return baos.size();
    }
}
