package com.info.hashing;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Cryptographic and non-cryptographic hash functions.
 *
 * <p>A hash function maps arbitrary-length input to a fixed-length digest.
 * It is <em>deterministic</em> (same input → same output) and ideally
 * <em>one-way</em> (infeasible to reverse) and <em>collision-resistant</em>
 * (infeasible to find two inputs with the same digest).
 *
 * <p>MD5 and SHA-1 are cryptographically broken — don't use them for
 * security. SHA-256/SHA-3 are current best practice.
 */
public final class Hashing {

    private Hashing() {}

    // ---------------------------------------------------------------
    // Digest functions
    // ---------------------------------------------------------------

    public static byte[] sha256(byte[] data) {
        return digest("SHA-256", data);
    }

    public static byte[] sha512(byte[] data) {
        return digest("SHA-512", data);
    }

    public static byte[] md5(byte[] data) {
        return digest("MD5", data);
    }

    /** Convenience overload: hashes a UTF-8 string, returns lowercase hex. */
    public static String sha256Hex(String text) {
        return toHex(sha256(text.getBytes(StandardCharsets.UTF_8)));
    }

    public static String md5Hex(String text) {
        return toHex(md5(text.getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] digest(String algorithm, byte[] data) {
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------
    // HMAC — keyed message authentication code
    // ---------------------------------------------------------------

    /**
     * Computes HMAC-SHA256: a keyed hash that proves both data integrity and
     * that the sender holds the key. Unlike a plain hash, an attacker who
     * doesn't know the key cannot forge a valid HMAC.
     */
    public static byte[] hmacSha256(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hmacSha256Hex(String key, String message) {
        return toHex(hmacSha256(
            key.getBytes(StandardCharsets.UTF_8),
            message.getBytes(StandardCharsets.UTF_8)));
    }

    // ---------------------------------------------------------------
    // Salted password hashing
    // ---------------------------------------------------------------

    /**
     * Returns a storable string of the form {@code hexSalt:hexHash}.
     *
     * <p>The random salt ensures two users with the same password get
     * different stored values, defeating pre-computed rainbow tables.
     * In production, use bcrypt/Argon2 instead — they are intentionally slow.
     */
    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = sha256(concat(salt, password.getBytes(StandardCharsets.UTF_8)));
        return toHex(salt) + ":" + toHex(hash);
    }

    public static boolean verifyPassword(String password, String stored) {
        String[] parts = stored.split(":", 2);
        byte[] salt    = fromHex(parts[0]);
        byte[] expected = fromHex(parts[1]);
        byte[] actual   = sha256(concat(salt, password.getBytes(StandardCharsets.UTF_8)));
        return MessageDigest.isEqual(actual, expected);
    }

    // ---------------------------------------------------------------
    // Consistent hashing — map a key to a position on a ring
    // ---------------------------------------------------------------

    /**
     * Maps an arbitrary string key to a position in {@code [0, ringSize)}.
     * Consistent hashing is used in distributed systems to assign keys to
     * nodes in a way that minimises reassignment when the node count changes.
     */
    public static long ringPosition(String key, long ringSize) {
        byte[] hash = sha256(key.getBytes(StandardCharsets.UTF_8));
        long pos = 0;
        for (int i = 0; i < 8; i++) pos = (pos << 8) | (hash[i] & 0xFFL);
        return Math.abs(pos) % ringSize;
    }

    // ---------------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------------

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b & 0xFF));
        return sb.toString();
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}
