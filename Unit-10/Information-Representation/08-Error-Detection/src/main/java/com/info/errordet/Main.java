package com.info.errordet;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Error Detection & Correction — Redundancy for Reliability ===\n");

        byte[] data = "Hello, World!".getBytes(StandardCharsets.UTF_8);

        // ---------------------------------------------------------------
        // 1. Even parity
        // ---------------------------------------------------------------
        System.out.println("--- 1. Even parity ---");
        byte parity = ErrorDetection.evenParity(data);
        System.out.printf("  Data   : %s%n", Arrays.toString(data));
        System.out.printf("  Parity : 0x%02X%n", parity & 0xFF);
        System.out.printf("  Valid  : %b%n", ErrorDetection.checkEvenParity(data, parity));

        byte[] corrupted = data.clone();
        corrupted[0] ^= 0x01;  // flip a bit in the first byte
        System.out.printf("  After 1-bit flip: detected=%b%n",
            !ErrorDetection.checkEvenParity(corrupted, parity));

        // ---------------------------------------------------------------
        // 2. Checksum-8
        // ---------------------------------------------------------------
        System.out.println("\n--- 2. Checksum-8 ---");
        byte cs = ErrorDetection.checksum8(data);
        System.out.printf("  Checksum  : 0x%02X%n", cs & 0xFF);
        System.out.printf("  Valid     : %b%n", ErrorDetection.verifyChecksum8(data, cs));
        System.out.printf("  Corrupted : %b%n", ErrorDetection.verifyChecksum8(corrupted, cs));

        // ---------------------------------------------------------------
        // 3. CRC-32
        // ---------------------------------------------------------------
        System.out.println("\n--- 3. CRC-32 ---");
        long crc = ErrorDetection.crc32(data);
        System.out.printf("  CRC-32 of data    : 0x%08X%n", crc);
        System.out.printf("  CRC-32 of corrupt : 0x%08X%n", ErrorDetection.crc32(corrupted));
        System.out.printf("  1-byte change detected: %b%n", crc != ErrorDetection.crc32(corrupted));

        // PNG uses CRC-32 for each chunk — demonstrate on known input
        byte[] pngChunk = "IHDR".getBytes(StandardCharsets.US_ASCII);
        System.out.printf("  CRC-32(\"IHDR\")    : 0x%08X%n", ErrorDetection.crc32(pngChunk));

        // ---------------------------------------------------------------
        // 4. Hamming(7,4)
        // ---------------------------------------------------------------
        System.out.println("\n--- 4. Hamming(7,4) ---");

        boolean[][] testCases = {
            {false, false, false, false},
            {true,  false, true,  true },
            {true,  true,  true,  true },
        };

        for (boolean[] d : testCases) {
            boolean[] encoded = ErrorDetection.hammingEncode(d);
            System.out.printf("  data=%s  encoded=%s%n", bitsStr(d), bitsStr(encoded));

            // Introduce a 1-bit error at position 3 (index 2)
            boolean[] noisy = encoded.clone();
            noisy[2] = !noisy[2];
            int errPos  = ErrorDetection.hammingErrorPosition(noisy);
            boolean[] corrected = ErrorDetection.hammingDecode(noisy);
            System.out.printf("    → flipped bit 3: error at pos=%d, recovered=%s, correct=%b%n",
                errPos, bitsStr(corrected), Arrays.equals(d, corrected));
        }
    }

    private static String bitsStr(boolean[] bits) {
        StringBuilder sb = new StringBuilder("[");
        for (boolean b : bits) sb.append(b ? "1" : "0");
        return sb.append("]").toString();
    }
}
