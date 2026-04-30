package com.info.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256 in CBC mode with PKCS5 padding — a modern symmetric cipher.
 *
 * <p>Key concepts:
 * <ul>
 *   <li><b>Symmetric</b> — the same key encrypts and decrypts. The key must be
 *       kept secret; if it leaks, all messages are compromised.
 *   <li><b>CBC mode</b> — each plaintext block is XOR'd with the previous
 *       ciphertext block before encryption. This means identical plaintext
 *       blocks produce different ciphertext blocks, hiding patterns.
 *   <li><b>IV (Initialization Vector)</b> — a random 16-byte value that seeds
 *       the first XOR in CBC. It must be unique per message but need not be
 *       secret; it is typically prepended to the ciphertext.
 *   <li><b>PKCS5 padding</b> — pads the final block to 16 bytes so AES (a
 *       block cipher) can process any plaintext length.
 * </ul>
 */
public final class AesCipher {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private AesCipher() {}

    public static SecretKey generateKey() throws GeneralSecurityException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, new SecureRandom());
        return kg.generateKey();
    }

    public static byte[] generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(plaintext);
    }

    public static byte[] decrypt(byte[] ciphertext, SecretKey key, byte[] iv)
            throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return cipher.doFinal(ciphertext);
    }

    /**
     * Encrypts a UTF-8 string and returns {@code Base64(iv) + ":" + Base64(ciphertext)}.
     * The IV is bundled with the ciphertext so the receiver can decrypt without
     * a separate channel for the IV.
     */
    public static String encryptString(String plaintext, SecretKey key)
            throws GeneralSecurityException {
        byte[] iv         = generateIv();
        byte[] ciphertext = encrypt(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8), key, iv);
        return Base64.getEncoder().encodeToString(iv)
             + ":" + Base64.getEncoder().encodeToString(ciphertext);
    }

    public static String decryptString(String encoded, SecretKey key)
            throws GeneralSecurityException {
        String[] parts    = encoded.split(":", 2);
        byte[] iv         = Base64.getDecoder().decode(parts[0]);
        byte[] ciphertext = Base64.getDecoder().decode(parts[1]);
        byte[] plaintext  = decrypt(ciphertext, key, iv);
        return new String(plaintext, java.nio.charset.StandardCharsets.UTF_8);
    }
}
