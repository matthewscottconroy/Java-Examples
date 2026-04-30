package com.info.hashing;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Hashing — Fixed-Length Fingerprints of Data ===\n");

        // ---------------------------------------------------------------
        // 1. Basic digests
        // ---------------------------------------------------------------
        System.out.println("--- SHA-256 digests ---");
        String[] inputs = { "", "hello", "hello", "Hello", "a".repeat(1_000_000) };
        for (String s : inputs) {
            String label = s.length() > 10 ? s.substring(0, 10) + "…" : "\"" + s + "\"";
            System.out.printf("  %-15s → %s%n", label, Hashing.sha256Hex(s));
        }

        // ---------------------------------------------------------------
        // 2. MD5 (broken for security, still used for checksums)
        // ---------------------------------------------------------------
        System.out.println("\n--- MD5 digests ---");
        System.out.println("  \"\"     → " + Hashing.md5Hex(""));
        System.out.println("  \"abc\"  → " + Hashing.md5Hex("abc"));

        // ---------------------------------------------------------------
        // 3. HMAC — keyed authentication code
        // ---------------------------------------------------------------
        System.out.println("\n--- HMAC-SHA256 ---");
        String key1 = "secret-key";
        String key2 = "other-key";
        String msg  = "invoice:1000:USD";
        System.out.println("  key1+msg : " + Hashing.hmacSha256Hex(key1, msg));
        System.out.println("  key2+msg : " + Hashing.hmacSha256Hex(key2, msg));
        System.out.println("  same keys same msg same result: " +
            Hashing.hmacSha256Hex(key1, msg).equals(Hashing.hmacSha256Hex(key1, msg)));

        // ---------------------------------------------------------------
        // 4. Salted password hashing
        // ---------------------------------------------------------------
        System.out.println("\n--- Salted password hashing ---");
        String stored1 = Hashing.hashPassword("p@ssw0rd");
        String stored2 = Hashing.hashPassword("p@ssw0rd");
        System.out.println("  hash1 : " + stored1);
        System.out.println("  hash2 : " + stored2);
        System.out.println("  different despite same password: " + !stored1.equals(stored2));
        System.out.println("  verify correct  : " + Hashing.verifyPassword("p@ssw0rd", stored1));
        System.out.println("  verify wrong    : " + Hashing.verifyPassword("wrong",    stored1));

        // ---------------------------------------------------------------
        // 5. Consistent hashing ring
        // ---------------------------------------------------------------
        System.out.println("\n--- Consistent hashing (ring size 360) ---");
        long ring = 360;
        String[] keys = { "user:1", "user:2", "order:99", "session:abc", "cache:x" };
        for (String k : keys)
            System.out.printf("  %-14s → position %3d%n", k, Hashing.ringPosition(k, ring));
    }
}
