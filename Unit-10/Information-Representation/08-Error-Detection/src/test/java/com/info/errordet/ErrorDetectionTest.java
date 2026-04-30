package com.info.errordet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ErrorDetectionTest {

    private static final byte[] HELLO = "Hello".getBytes(StandardCharsets.UTF_8);

    // -- Even parity --

    @Test @DisplayName("evenParity of all-zero bytes is 0")
    void parity_allZero() {
        assertEquals(0, ErrorDetection.evenParity(new byte[]{0, 0, 0, 0}));
    }

    @Test @DisplayName("checkEvenParity passes for intact data")
    void parity_intact() {
        byte p = ErrorDetection.evenParity(HELLO);
        assertTrue(ErrorDetection.checkEvenParity(HELLO, p));
    }

    @Test @DisplayName("checkEvenParity fails after 1-bit flip")
    void parity_flippedBit() {
        byte p = ErrorDetection.evenParity(HELLO);
        byte[] bad = HELLO.clone(); bad[0] ^= 0x01;
        assertFalse(ErrorDetection.checkEvenParity(bad, p));
    }

    @Test @DisplayName("evenParity of identical consecutive bytes is 0")
    void parity_cancelsPairs() {
        assertEquals(0, ErrorDetection.evenParity(new byte[]{0x42, 0x42}));
    }

    // -- Checksum-8 --

    @Test @DisplayName("verifyChecksum8 passes for intact data")
    void cs_intact() {
        byte cs = ErrorDetection.checksum8(HELLO);
        assertTrue(ErrorDetection.verifyChecksum8(HELLO, cs));
    }

    @Test @DisplayName("verifyChecksum8 fails after single-byte change")
    void cs_corrupted() {
        byte cs = ErrorDetection.checksum8(HELLO);
        byte[] bad = HELLO.clone(); bad[1]++;
        assertFalse(ErrorDetection.verifyChecksum8(bad, cs));
    }

    @Test @DisplayName("checksum8 of all-zero bytes is 0xFF")
    void cs_allZero() {
        byte cs = ErrorDetection.checksum8(new byte[]{0, 0, 0});
        assertEquals((byte) 0xFF, cs);
    }

    // -- CRC-32 --

    @Test @DisplayName("CRC-32 is deterministic")
    void crc_deterministic() {
        assertEquals(ErrorDetection.crc32(HELLO), ErrorDetection.crc32(HELLO));
    }

    @Test @DisplayName("CRC-32 of empty array is 0")
    void crc_empty() {
        assertEquals(0L, ErrorDetection.crc32(new byte[0]));
    }

    @Test @DisplayName("CRC-32 changes when any byte changes")
    void crc_detectsChange() {
        long crc = ErrorDetection.crc32(HELLO);
        byte[] bad = HELLO.clone(); bad[0] ^= 0xFF;
        assertNotEquals(crc, ErrorDetection.crc32(bad));
    }

    @Test @DisplayName("CRC-32 result is a 32-bit unsigned value (0..2^32-1)")
    void crc_range() {
        long crc = ErrorDetection.crc32("test".getBytes());
        assertTrue(crc >= 0 && crc <= 0xFFFFFFFFL);
    }

    // -- Hamming(7,4) --

    @Test @DisplayName("hammingEncode returns 7 bits")
    void hamming_encodeLength() {
        assertEquals(7, ErrorDetection.hammingEncode(new boolean[]{true,false,true,false}).length);
    }

    @Test @DisplayName("hammingDecode recovers original data (no error)")
    void hamming_roundTrip() {
        boolean[] data    = {true, false, true, true};
        boolean[] encoded = ErrorDetection.hammingEncode(data);
        boolean[] decoded = ErrorDetection.hammingDecode(encoded);
        assertArrayEquals(data, decoded);
    }

    @Test @DisplayName("hammingDecode corrects a single-bit error at each position")
    void hamming_correctsSingleBitError() {
        boolean[] data    = {true, false, true, false};
        boolean[] encoded = ErrorDetection.hammingEncode(data);

        for (int pos = 0; pos < 7; pos++) {
            boolean[] noisy = encoded.clone();
            noisy[pos] = !noisy[pos];  // flip one bit
            boolean[] recovered = ErrorDetection.hammingDecode(noisy);
            assertArrayEquals(data, recovered,
                "Failed to correct error at position " + pos);
        }
    }

    @Test @DisplayName("hammingErrorPosition returns 0 when there is no error")
    void hamming_noError() {
        boolean[] encoded = ErrorDetection.hammingEncode(new boolean[]{true,true,false,true});
        assertEquals(0, ErrorDetection.hammingErrorPosition(encoded));
    }

    @Test @DisplayName("hammingErrorPosition identifies the correct error position")
    void hamming_errorPosition() {
        boolean[] data    = {false, true, false, false};
        boolean[] encoded = ErrorDetection.hammingEncode(data);

        for (int pos = 0; pos < 7; pos++) {
            boolean[] noisy = encoded.clone();
            noisy[pos] = !noisy[pos];
            // Position is 1-indexed: pos+1
            assertEquals(pos + 1, ErrorDetection.hammingErrorPosition(noisy),
                "Wrong error position for flip at index " + pos);
        }
    }

    @Test @DisplayName("hammingEncode throws for wrong data length")
    void hamming_wrongInput() {
        assertThrows(IllegalArgumentException.class,
            () -> ErrorDetection.hammingEncode(new boolean[]{true, false}));
    }

    @Test @DisplayName("hammingDecode throws for wrong codeword length")
    void hamming_wrongCodeword() {
        assertThrows(IllegalArgumentException.class,
            () -> ErrorDetection.hammingDecode(new boolean[]{true, false, true}));
    }

    @Test @DisplayName("All-zero data encodes to all-zero codeword")
    void hamming_allZero() {
        boolean[] data    = {false, false, false, false};
        boolean[] encoded = ErrorDetection.hammingEncode(data);
        assertArrayEquals(new boolean[7], encoded);
    }
}
