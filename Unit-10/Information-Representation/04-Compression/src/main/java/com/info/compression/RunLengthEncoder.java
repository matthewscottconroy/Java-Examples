package com.info.compression;

/**
 * Run-length encoding (RLE) — the simplest lossless compression scheme.
 *
 * <p>Consecutive runs of the same character are replaced by a count and
 * the character: {@code "AAABBC" → "3A2B1C"}. RLE is effective for data
 * with long repeated runs (fax images, simple bitmaps) but can actually
 * <em>expand</em> data that has no repetition ({@code "ABC" → "1A1B1C"}).
 */
public final class RunLengthEncoder {

    private RunLengthEncoder() {}

    public static String encode(String s) {
        if (s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            int count = 0;
            while (i < s.length() && s.charAt(i) == c) { count++; i++; }
            sb.append(count).append(c);
        }
        return sb.toString();
    }

    public static String decode(String encoded) {
        if (encoded.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < encoded.length()) {
            int start = i;
            while (i < encoded.length() && Character.isDigit(encoded.charAt(i))) i++;
            int count = Integer.parseInt(encoded.substring(start, i));
            char c = encoded.charAt(i++);
            sb.append(String.valueOf(c).repeat(count));
        }
        return sb.toString();
    }
}
