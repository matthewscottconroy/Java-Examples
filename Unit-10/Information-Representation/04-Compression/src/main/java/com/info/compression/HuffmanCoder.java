package com.info.compression;

import java.util.*;

/**
 * Huffman coding — optimal prefix-free compression for a known symbol distribution.
 *
 * <p>The algorithm builds a binary tree where frequently used characters sit
 * near the root (short codes) and rare characters sit near the leaves (long
 * codes). Because no code is a prefix of another, the bit stream can be
 * decoded unambiguously without delimiters.
 *
 * <p>The code table is stored alongside the compressed bits so the receiver
 * can reconstruct the tree and decode.
 */
public final class HuffmanCoder {

    // ---------------------------------------------------------------
    // Internal tree nodes (private to this class)
    // ---------------------------------------------------------------

    private sealed interface HuffNode permits HuffLeaf, HuffBranch {
        int freq();
    }

    private record HuffLeaf(char symbol, int freq) implements HuffNode {}

    private record HuffBranch(int freq, HuffNode left, HuffNode right)
            implements HuffNode {}

    // ---------------------------------------------------------------
    // Public result type
    // ---------------------------------------------------------------

    /**
     * @param bits           binary string ("010110…") of the compressed data
     * @param codeTable      maps each character to its Huffman bit-string
     * @param originalLength character count of the original string
     */
    public record CompressedData(
            String bits,
            Map<Character, String> codeTable,
            int originalLength) {

        /** Ratio > 1 means compressed; = 1 means same size; < 1 means expanded. */
        public double compressionRatio() {
            int compressedBits = bits.length();
            if (compressedBits == 0) return 1.0;
            return (double) (originalLength * 8) / compressedBits;
        }
    }

    private HuffmanCoder() {}

    // ---------------------------------------------------------------
    // Compression
    // ---------------------------------------------------------------

    public static CompressedData compress(String text) {
        if (text.isEmpty()) return new CompressedData("", Map.of(), 0);

        // Frequency table
        Map<Character, Integer> freq = new HashMap<>();
        for (char c : text.toCharArray()) freq.merge(c, 1, Integer::sum);

        // Single distinct character: trivial tree
        if (freq.size() == 1) {
            char only = freq.keySet().iterator().next();
            Map<Character, String> codes = Map.of(only, "0");
            return new CompressedData("0".repeat(text.length()), codes, text.length());
        }

        // Build min-heap and merge until one root remains
        PriorityQueue<HuffNode> pq =
            new PriorityQueue<>(Comparator.comparingInt(HuffNode::freq));
        freq.forEach((ch, f) -> pq.add(new HuffLeaf(ch, f)));

        while (pq.size() > 1) {
            HuffNode left  = pq.poll();
            HuffNode right = pq.poll();
            pq.add(new HuffBranch(left.freq() + right.freq(), left, right));
        }

        // Generate code table via depth-first traversal
        Map<Character, String> codes = new HashMap<>();
        buildCodes(pq.poll(), "", codes);

        // Encode
        StringBuilder bits = new StringBuilder();
        for (char c : text.toCharArray()) bits.append(codes.get(c));

        return new CompressedData(bits.toString(), Map.copyOf(codes), text.length());
    }

    private static void buildCodes(HuffNode node, String prefix, Map<Character, String> codes) {
        switch (node) {
            case HuffLeaf l   -> codes.put(l.symbol(), prefix);
            case HuffBranch b -> {
                buildCodes(b.left(),  prefix + "0", codes);
                buildCodes(b.right(), prefix + "1", codes);
            }
        }
    }

    // ---------------------------------------------------------------
    // Decompression
    // ---------------------------------------------------------------

    /**
     * Decodes using the reverse code table — works because Huffman codes are
     * prefix-free: as soon as a bit sequence matches a code, it must be that symbol.
     */
    public static String decompress(CompressedData data) {
        if (data.bits().isEmpty()) return "";

        Map<String, Character> reverse = new HashMap<>();
        data.codeTable().forEach((ch, code) -> reverse.put(code, ch));

        StringBuilder result  = new StringBuilder();
        StringBuilder current = new StringBuilder();
        for (char bit : data.bits().toCharArray()) {
            current.append(bit);
            Character ch = reverse.get(current.toString());
            if (ch != null) {
                result.append(ch);
                current.setLength(0);
            }
        }
        return result.toString();
    }
}
