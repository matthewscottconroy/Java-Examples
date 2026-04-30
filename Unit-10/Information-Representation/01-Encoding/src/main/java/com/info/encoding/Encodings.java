package com.info.encoding;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Common encoding and decoding schemes used to represent binary data as text
 * and to detect data formats from their raw bytes.
 *
 * <p>Encoding is NOT encryption — it transforms data so it can be safely
 * transported through text-only channels (Base64 over email, hex in logs,
 * URL-encoded query strings) or stored in a particular format. Anyone who
 * knows the scheme can reverse it without a key.
 */
public final class Encodings {

    private static final String HEX_CHARS = "0123456789abcdef";

    private Encodings() {}

    // ---------------------------------------------------------------
    // Base64
    // ---------------------------------------------------------------

    /** Encodes bytes to standard Base64 (with padding). */
    public static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /** Decodes standard Base64 to bytes. */
    public static byte[] fromBase64(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }

    /** URL-safe Base64 (- and _ instead of + and /), no padding. Used in JWTs. */
    public static String toBase64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static byte[] fromBase64Url(String encoded) {
        return Base64.getUrlDecoder().decode(encoded);
    }

    // ---------------------------------------------------------------
    // Hex
    // ---------------------------------------------------------------

    /** Converts bytes to lowercase hex string. Each byte → two hex digits. */
    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_CHARS.charAt((b >> 4) & 0xF));
            sb.append(HEX_CHARS.charAt(b & 0xF));
        }
        return sb.toString();
    }

    /** Converts a hex string back to bytes. String length must be even. */
    public static byte[] fromHex(String hex) {
        if (hex.length() % 2 != 0) throw new IllegalArgumentException("Odd hex length");
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    // ---------------------------------------------------------------
    // URL encoding
    // ---------------------------------------------------------------

    /** Percent-encodes a string for safe use in a URL query parameter. */
    public static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** Decodes a percent-encoded string. */
    public static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------
    // Charset transcoding
    // ---------------------------------------------------------------

    public static byte[] encode(String s, Charset charset) {
        return s.getBytes(charset);
    }

    public static String decode(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    /**
     * Counts the bytes each encoding would use for the given string.
     * UTF-8 is variable-width; UTF-16 always uses 2 bytes per code unit.
     */
    public static Map<String, Integer> encodingWidths(String s) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("UTF-8",      s.getBytes(StandardCharsets.UTF_8).length);
        map.put("UTF-16BE",   s.getBytes(StandardCharsets.UTF_16BE).length);
        map.put("ISO-8859-1", s.getBytes(StandardCharsets.ISO_8859_1).length);
        return map;
    }

    // ---------------------------------------------------------------
    // Unicode code point inspection
    // ---------------------------------------------------------------

    /**
     * Returns a human-readable description of each Unicode code point in the string.
     * Useful for understanding multi-byte characters and surrogate pairs.
     */
    public static List<String> codePointDescriptions(String s) {
        List<String> result = new ArrayList<>();
        s.codePoints().forEach(cp -> result.add(
            String.format("U+%04X '%s' %s",
                cp, Character.toString(cp), Character.getName(cp))));
        return result;
    }

    // ---------------------------------------------------------------
    // Magic-byte file-type detection
    // ---------------------------------------------------------------

    /**
     * Identifies a file's type from its leading magic bytes (file signature).
     * File type cannot be reliably determined from the extension alone — the
     * magic bytes embedded in the data are the authoritative source.
     */
    public static String detectFileType(byte[] header) {
        if (startsWith(header, 0x89, 0x50, 0x4E, 0x47))          return "PNG";
        if (startsWith(header, 0xFF, 0xD8, 0xFF))                 return "JPEG";
        if (startsWith(header, 0x25, 0x50, 0x44, 0x46))          return "PDF";
        if (startsWith(header, 0x50, 0x4B, 0x03, 0x04))          return "ZIP";
        if (startsWith(header, 0x47, 0x49, 0x46, 0x38))          return "GIF";
        if (startsWith(header, 0x42, 0x4D))                       return "BMP";
        if (startsWith(header, 0x1F, 0x8B))                       return "GZIP";
        if (startsWith(header, 0x7F, 0x45, 0x4C, 0x46))          return "ELF";
        if (startsWith(header, 0xEF, 0xBB, 0xBF))                return "UTF-8 BOM";
        if (startsWith(header, 0xFF, 0xFE))                       return "UTF-16 LE BOM";
        if (startsWith(header, 0xFE, 0xFF))                       return "UTF-16 BE BOM";
        return "UNKNOWN";
    }

    private static boolean startsWith(byte[] data, int... expected) {
        if (data.length < expected.length) return false;
        for (int i = 0; i < expected.length; i++)
            if ((data[i] & 0xFF) != expected[i]) return false;
        return true;
    }
}
