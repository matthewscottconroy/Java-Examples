package com.info.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionTest {

    // -- Caesar --

    @Test @DisplayName("Caesar encrypt/decrypt round-trip")
    void caesar_roundTrip() {
        String plain = "Hello, World!";
        assertEquals(plain, Caesar.decrypt(Caesar.encrypt(plain, 7), 7));
    }

    @Test @DisplayName("Caesar shift 0 is identity")
    void caesar_shiftZero() {
        assertEquals("abc", Caesar.encrypt("abc", 0));
    }

    @Test @DisplayName("Caesar shift 26 is identity")
    void caesar_shift26() {
        assertEquals("xyz", Caesar.encrypt("xyz", 26));
    }

    @Test @DisplayName("Caesar preserves non-letter characters")
    void caesar_preservesNonLetters() {
        String s = "Hello, World! 123";
        String enc = Caesar.encrypt(s, 5);
        assertTrue(enc.contains(",") && enc.contains("!") && enc.contains(" ") && enc.contains("123"));
    }

    @Test @DisplayName("Caesar preserves case")
    void caesar_preservesCase() {
        String enc = Caesar.encrypt("AbCd", 1);
        assertEquals("BcDe", enc);
    }

    @Test @DisplayName("Caesar with negative shift (equivalent to positive)")
    void caesar_negativeShift() {
        String enc = Caesar.encrypt("abc", -1);
        assertEquals("zab", enc);
    }

    @Test @DisplayName("ROT13 applied twice is identity")
    void caesar_rot13Involution() {
        String s = "The quick brown fox";
        assertEquals(s, Caesar.encrypt(Caesar.encrypt(s, 13), 13));
    }

    @Test @DisplayName("Caesar brute-force contains correct plaintext")
    void caesar_bruteForce() {
        String plain = "meet at noon";
        int shift = 11;
        String cipher = Caesar.encrypt(plain, shift);
        Map<Integer, String> candidates = Caesar.bruteForce(cipher);
        assertTrue(candidates.containsValue(plain));
    }

    @Test @DisplayName("Caesar brute-force returns exactly 26 entries")
    void caesar_bruteForceSize() {
        Map<Integer, String> r = Caesar.bruteForce(Caesar.encrypt("hi", 3));
        assertEquals(26, r.size());
    }

    // -- AES --

    @Test @DisplayName("AES encrypt/decrypt round-trip (byte arrays)")
    void aes_byteRoundTrip() throws Exception {
        SecretKey key = AesCipher.generateKey();
        byte[] iv        = AesCipher.generateIv();
        byte[] plaintext = "secret message".getBytes(StandardCharsets.UTF_8);
        byte[] cipher    = AesCipher.encrypt(plaintext, key, iv);
        assertArrayEquals(plaintext, AesCipher.decrypt(cipher, key, iv));
    }

    @Test @DisplayName("AES ciphertext differs from plaintext")
    void aes_ciphertextDiffers() throws Exception {
        SecretKey key    = AesCipher.generateKey();
        byte[] iv        = AesCipher.generateIv();
        byte[] plaintext = "hello world!!!!".getBytes(StandardCharsets.UTF_8);
        byte[] cipher    = AesCipher.encrypt(plaintext, key, iv);
        assertFalse(java.util.Arrays.equals(plaintext, cipher));
    }

    @Test @DisplayName("AES string encrypt/decrypt round-trip")
    void aes_stringRoundTrip() throws Exception {
        SecretKey key = AesCipher.generateKey();
        String message = "Confidential payload with Unicode: café";
        assertEquals(message, AesCipher.decryptString(AesCipher.encryptString(message, key), key));
    }

    @Test @DisplayName("Same plaintext encrypted twice gives different ciphertexts (random IV)")
    void aes_randomIvDiffers() throws Exception {
        SecretKey key = AesCipher.generateKey();
        String message = "same plaintext";
        assertNotEquals(
            AesCipher.encryptString(message, key),
            AesCipher.encryptString(message, key));
    }

    @Test @DisplayName("AES IV is 16 bytes")
    void aes_ivLength() {
        assertEquals(16, AesCipher.generateIv().length);
    }

    @Test @DisplayName("AES key is 256-bit (32 bytes)")
    void aes_keyLength() throws Exception {
        SecretKey key = AesCipher.generateKey();
        assertEquals(32, key.getEncoded().length);
    }
}
