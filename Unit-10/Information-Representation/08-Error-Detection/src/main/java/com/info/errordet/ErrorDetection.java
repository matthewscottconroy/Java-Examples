package com.info.errordet;

import java.util.zip.CRC32;

/**
 * Error detection and correction schemes — adding redundancy to data so that
 * corruption in transit can be detected (and sometimes fixed).
 *
 * <p>All schemes trade extra bits for reliability:
 * <ul>
 *   <li><b>Even parity</b> — 1 extra bit per block; detects 1 flipped bit, corrects 0.
 *   <li><b>Checksum-8</b> — 1 byte; detects most burst errors in a block.
 *   <li><b>CRC-32</b> — 4 bytes; detects all single-bit errors, most burst errors up to
 *       32 bits wide; used in Ethernet, ZIP, PNG.
 *   <li><b>Hamming(7,4)</b> — encodes 4 data bits in 7 bits (3 parity bits);
 *       corrects any single-bit error and detects any two-bit error.
 * </ul>
 */
public final class ErrorDetection {

    private ErrorDetection() {}

    // ---------------------------------------------------------------
    // 1. Even parity
    // ---------------------------------------------------------------

    /**
     * Computes an even-parity byte: the XOR of all bytes in {@code data}.
     * XORing the parity byte back with all data bytes yields 0 if no bits flipped.
     */
    public static byte evenParity(byte[] data) {
        byte p = 0;
        for (byte b : data) p ^= b;
        return p;
    }

    /** Returns true if the data matches the stored parity byte (no detected error). */
    public static boolean checkEvenParity(byte[] data, byte parity) {
        return evenParity(data) == parity;
    }

    // ---------------------------------------------------------------
    // 2. One's-complement checksum
    // ---------------------------------------------------------------

    /**
     * Computes a simple 8-bit checksum: sum all bytes modulo 256, then
     * take the one's complement. Adding the checksum to the sum yields 0xFF
     * if no errors occurred. Used in IP header checksums (16-bit variant).
     */
    public static byte checksum8(byte[] data) {
        int sum = 0;
        for (byte b : data) sum += (b & 0xFF);
        return (byte) (~(sum & 0xFF));
    }

    public static boolean verifyChecksum8(byte[] data, byte checksum) {
        int sum = 0;
        for (byte b : data) sum += (b & 0xFF);
        sum += (checksum & 0xFF);
        return (sum & 0xFF) == 0xFF;
    }

    // ---------------------------------------------------------------
    // 3. CRC-32
    // ---------------------------------------------------------------

    /**
     * CRC-32 as used in Ethernet, ZIP, and PNG.
     * A cyclic redundancy check treats the data as a polynomial and divides by
     * a generator polynomial; the remainder is the checksum. Any 1-bit error
     * is guaranteed to be detected; burst errors up to 32 bits wide are caught.
     */
    public static long crc32(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    // ---------------------------------------------------------------
    // 4. Hamming(7,4) — single-error correction
    // ---------------------------------------------------------------
    //
    // Bit positions 1–7 (1-indexed); parity bits at powers of 2: p1=1, p2=2, p4=4.
    // Data bits at positions 3, 5, 6, 7 → array indices 2, 4, 5, 6.
    //
    //   pos:   1   2   3   4   5   6   7
    //   role: p1  p2  d1  p4  d2  d3  d4
    //   idx:   0   1   2   3   4   5   6
    //
    // Each parity bit covers all positions where its bit position is set:
    //   p1 (bit 1): positions 1,3,5,7 → indices 0,2,4,6
    //   p2 (bit 2): positions 2,3,6,7 → indices 1,2,5,6
    //   p4 (bit 4): positions 4,5,6,7 → indices 3,4,5,6

    /**
     * Encodes 4 data bits as a 7-bit Hamming codeword.
     * @param data exactly 4 booleans (data bits d1…d4)
     * @return 7-bit codeword [p1, p2, d1, p4, d2, d3, d4]
     */
    public static boolean[] hammingEncode(boolean[] data) {
        if (data.length != 4) throw new IllegalArgumentException("Hamming(7,4) needs exactly 4 data bits");
        boolean[] code = new boolean[7];
        code[2] = data[0]; // d1
        code[4] = data[1]; // d2
        code[5] = data[2]; // d3
        code[6] = data[3]; // d4
        // Parity bits (even parity over their groups)
        code[0] = code[2] ^ code[4] ^ code[6]; // p1
        code[1] = code[2] ^ code[5] ^ code[6]; // p2
        code[3] = code[4] ^ code[5] ^ code[6]; // p4
        return code;
    }

    /**
     * Decodes and corrects a 7-bit Hamming codeword, fixing any single-bit error.
     * @param received 7 bits (possibly with 1 flipped bit)
     * @return 4 recovered data bits
     */
    public static boolean[] hammingDecode(boolean[] received) {
        if (received.length != 7) throw new IllegalArgumentException("Hamming(7,4) needs exactly 7 bits");
        boolean[] code = received.clone();

        // Syndrome: XOR each parity group; if non-zero, the syndrome value is the error position
        int s1 = (code[0] ^ code[2] ^ code[4] ^ code[6]) ? 1 : 0;
        int s2 = (code[1] ^ code[2] ^ code[5] ^ code[6]) ? 2 : 0;
        int s4 = (code[3] ^ code[4] ^ code[5] ^ code[6]) ? 4 : 0;
        int syndrome = s1 | s2 | s4;

        if (syndrome != 0) code[syndrome - 1] = !code[syndrome - 1]; // correct the bit

        return new boolean[]{code[2], code[4], code[5], code[6]};
    }

    /** Returns the 1-based error position (0 = no error). */
    public static int hammingErrorPosition(boolean[] received) {
        if (received.length != 7) throw new IllegalArgumentException("Hamming(7,4) needs exactly 7 bits");
        int s1 = (received[0] ^ received[2] ^ received[4] ^ received[6]) ? 1 : 0;
        int s2 = (received[1] ^ received[2] ^ received[5] ^ received[6]) ? 2 : 0;
        int s4 = (received[3] ^ received[4] ^ received[5] ^ received[6]) ? 4 : 0;
        return s1 | s2 | s4;
    }
}
