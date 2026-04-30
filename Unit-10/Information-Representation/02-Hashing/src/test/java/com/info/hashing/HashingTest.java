package com.info.hashing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashingTest {

    // -- SHA-256 --

    @Test @DisplayName("SHA-256 of empty string matches known value")
    void sha256_emptyString() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            Hashing.sha256Hex(""));
    }

    @Test @DisplayName("SHA-256 of 'abc' matches known value")
    void sha256_abc() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Hashing.sha256Hex("abc"));
    }

    @Test @DisplayName("SHA-256 is deterministic")
    void sha256_deterministic() {
        assertEquals(Hashing.sha256Hex("hello"), Hashing.sha256Hex("hello"));
    }

    @Test @DisplayName("SHA-256 differs for different inputs")
    void sha256_distinctInputs() {
        assertNotEquals(Hashing.sha256Hex("hello"), Hashing.sha256Hex("Hello"));
    }

    @Test @DisplayName("SHA-256 digest is 32 bytes (256 bits)")
    void sha256_length() {
        assertEquals(32, Hashing.sha256("test".getBytes()).length);
    }

    // -- MD5 --

    @Test @DisplayName("MD5 of empty string matches known value")
    void md5_empty() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Hashing.md5Hex(""));
    }

    @Test @DisplayName("MD5 digest is 16 bytes")
    void md5_length() {
        assertEquals(16, Hashing.md5("x".getBytes()).length);
    }

    // -- HMAC --

    @Test @DisplayName("HMAC same key+message gives same result")
    void hmac_deterministic() {
        assertEquals(
            Hashing.hmacSha256Hex("key", "msg"),
            Hashing.hmacSha256Hex("key", "msg"));
    }

    @Test @DisplayName("HMAC differs with different keys")
    void hmac_differentKeys() {
        assertNotEquals(
            Hashing.hmacSha256Hex("key1", "msg"),
            Hashing.hmacSha256Hex("key2", "msg"));
    }

    @Test @DisplayName("HMAC differs with different messages")
    void hmac_differentMessages() {
        assertNotEquals(
            Hashing.hmacSha256Hex("key", "msg1"),
            Hashing.hmacSha256Hex("key", "msg2"));
    }

    @Test @DisplayName("HMAC-SHA256 output is 32 bytes")
    void hmac_length() {
        assertEquals(32, Hashing.hmacSha256("k".getBytes(), "m".getBytes()).length);
    }

    // -- Password hashing --

    @Test @DisplayName("verifyPassword returns true for correct password")
    void password_verifyCorrect() {
        String stored = Hashing.hashPassword("mySecret");
        assertTrue(Hashing.verifyPassword("mySecret", stored));
    }

    @Test @DisplayName("verifyPassword returns false for wrong password")
    void password_verifyWrong() {
        String stored = Hashing.hashPassword("mySecret");
        assertFalse(Hashing.verifyPassword("wrong", stored));
    }

    @Test @DisplayName("Same password produces different stored values (random salt)")
    void password_saltedDiffers() {
        String s1 = Hashing.hashPassword("same");
        String s2 = Hashing.hashPassword("same");
        assertNotEquals(s1, s2);
    }

    // -- Consistent hashing --

    @Test @DisplayName("ringPosition is within [0, ringSize)")
    void ring_inBounds() {
        long size = 1000;
        for (String key : new String[]{"a", "b", "user:1", "order:99", ""}) {
            long pos = Hashing.ringPosition(key, size);
            assertTrue(pos >= 0 && pos < size, "position " + pos + " out of range");
        }
    }

    @Test @DisplayName("ringPosition is deterministic")
    void ring_deterministic() {
        assertEquals(Hashing.ringPosition("user:42", 360), Hashing.ringPosition("user:42", 360));
    }
}
