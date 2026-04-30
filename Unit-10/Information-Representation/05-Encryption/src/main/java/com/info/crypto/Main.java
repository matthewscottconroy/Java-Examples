package com.info.crypto;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Encryption — Hiding the Meaning of Data ===\n");

        // ---------------------------------------------------------------
        // 1. Caesar cipher
        // ---------------------------------------------------------------
        System.out.println("--- Caesar cipher (shift 13 = ROT13) ---");
        String plaintext = "The Quick Brown Fox Jumps Over The Lazy Dog";
        int shift = 13;
        String ciphertext = Caesar.encrypt(plaintext, shift);
        String decrypted  = Caesar.decrypt(ciphertext, shift);
        System.out.println("  Plaintext : " + plaintext);
        System.out.println("  Ciphertext: " + ciphertext);
        System.out.println("  Decrypted : " + decrypted);
        System.out.println("  ROT13(ROT13(x)) = x: " + Caesar.encrypt(ciphertext, 13).equals(plaintext));

        // ---------------------------------------------------------------
        // 2. Brute-force attack on Caesar
        // ---------------------------------------------------------------
        System.out.println("\n--- Brute-force (try all 26 shifts) ---");
        String secret = Caesar.encrypt("meet at dawn", 7);
        System.out.println("  Ciphertext: " + secret);
        Map<Integer, String> candidates = Caesar.bruteForce(secret);
        candidates.forEach((k, v) -> System.out.printf("  shift %2d: %s%n", k, v));

        // ---------------------------------------------------------------
        // 3. Frequency analysis
        // ---------------------------------------------------------------
        System.out.println("\n--- Letter frequencies in Caesar ciphertext ---");
        String longCipher = Caesar.encrypt(
            "to be or not to be that is the question whether tis nobler", 3);
        Caesar.letterFrequencies(longCipher)
            .entrySet().stream()
            .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> System.out.printf("  '%c' = %d%n", e.getKey(), e.getValue()));

        // ---------------------------------------------------------------
        // 4. AES-256-CBC
        // ---------------------------------------------------------------
        System.out.println("\n--- AES-256-CBC ---");
        SecretKey key = AesCipher.generateKey();
        String message = "Confidential: launch codes are 4-8-15-16-23-42";

        String encoded = AesCipher.encryptString(message, key);
        String recovered = AesCipher.decryptString(encoded, key);
        System.out.println("  Plaintext : " + message);
        System.out.println("  Encrypted : " + encoded);
        System.out.println("  Decrypted : " + recovered);

        // Same plaintext + different IV → different ciphertext each time
        String enc1 = AesCipher.encryptString(message, key);
        String enc2 = AesCipher.encryptString(message, key);
        System.out.println("  Same msg, different IVs produce different ciphertexts: " + !enc1.equals(enc2));
    }
}
