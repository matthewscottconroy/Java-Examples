package com.info.crypto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Caesar cipher — the simplest classical substitution cipher.
 *
 * <p>Each letter is shifted by a fixed amount (the key) modulo 26.
 * It provides no real security: with only 26 possible keys, an attacker
 * can try all of them by hand. It illustrates the core concept of symmetric
 * encryption: the same key is used to encrypt and decrypt.
 *
 * <p>Non-alphabetic characters (spaces, punctuation, digits) pass through
 * unchanged so the word structure is preserved — a weakness exploited by
 * frequency analysis.
 */
public final class Caesar {

    private Caesar() {}

    public static String encrypt(String text, int shift) {
        shift = normalize(shift);
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                char base = Character.isUpperCase(c) ? 'A' : 'a';
                sb.append((char) (base + (c - base + shift) % 26));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String decrypt(String cipher, int shift) {
        return encrypt(cipher, 26 - normalize(shift));
    }

    /**
     * Returns all 26 possible decryptions. One of them is the plaintext;
     * a human (or frequency analyser) can spot it without knowing the key.
     */
    public static Map<Integer, String> bruteForce(String cipher) {
        Map<Integer, String> results = new LinkedHashMap<>();
        for (int shift = 0; shift < 26; shift++)
            results.put(shift, decrypt(cipher, shift));
        return results;
    }

    /**
     * Counts letter frequencies. English text has a characteristic
     * distribution ('e' is most common at ~13%); matching this to the
     * cipher alphabet reveals the shift without trying all 26 keys.
     */
    public static Map<Character, Integer> letterFrequencies(String text) {
        Map<Character, Integer> freq = new LinkedHashMap<>();
        for (char c : text.toLowerCase().toCharArray())
            if (Character.isLetter(c)) freq.merge(c, 1, Integer::sum);
        return freq;
    }

    private static int normalize(int shift) {
        return ((shift % 26) + 26) % 26;
    }
}
