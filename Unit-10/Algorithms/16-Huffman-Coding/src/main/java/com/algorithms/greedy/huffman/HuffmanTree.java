package com.algorithms.greedy.huffman;

import java.util.*;

/**
 * Huffman coding: a greedy algorithm for lossless data compression.
 *
 * <p>Characters that appear more frequently get shorter bit codes;
 * infrequent characters get longer codes. The average code length is
 * minimised over all prefix-free binary codes.
 *
 * <p>Time: O(n log n) — n insertions and extractions from a min-heap.
 */
public final class HuffmanTree {

    private HuffmanTree() {}

    /** Internal tree node. Leaves hold a character; internal nodes do not. */
    private record Node(char ch, int freq, Node left, Node right) {
        boolean isLeaf() { return left == null && right == null; }
    }

    public record CodeTable(Map<Character, String> codes, Node root) {

        /** Returns the Huffman code for a character. */
        public String codeFor(char c) { return codes.get(c); }

        /** Returns all character → code mappings. */
        public Map<Character, String> allCodes() { return Collections.unmodifiableMap(codes); }

        /** Encodes the given text using the code table. */
        public String encode(String text) {
            StringBuilder sb = new StringBuilder();
            for (char c : text.toCharArray()) sb.append(codes.get(c));
            return sb.toString();
        }

        /** Decodes a bit string using the Huffman tree. */
        public String decode(String bits) {
            if (root.isLeaf()) {
                // Edge case: single distinct character
                return String.valueOf(root.ch()).repeat(bits.length());
            }
            StringBuilder sb = new StringBuilder();
            Node cur = root;
            for (char bit : bits.toCharArray()) {
                cur = bit == '0' ? cur.left() : cur.right();
                if (cur.isLeaf()) { sb.append(cur.ch()); cur = root; }
            }
            return sb.toString();
        }
    }

    /**
     * Builds a Huffman code table from character frequency counts.
     */
    public static CodeTable build(Map<Character, Integer> frequencies) {
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(Node::freq));
        for (var entry : frequencies.entrySet()) {
            pq.offer(new Node(entry.getKey(), entry.getValue(), null, null));
        }

        // Edge case: single character
        if (pq.size() == 1) {
            Node leaf = pq.poll();
            Map<Character, String> codes = new HashMap<>();
            codes.put(leaf.ch(), "0");
            Node root = new Node('\0', leaf.freq(), leaf, null);
            return new CodeTable(codes, root);
        }

        while (pq.size() > 1) {
            Node left  = pq.poll();
            Node right = pq.poll();
            pq.offer(new Node('\0', left.freq() + right.freq(), left, right));
        }

        Node root = pq.poll();
        Map<Character, String> codes = new HashMap<>();
        buildCodes(root, "", codes);
        return new CodeTable(codes, root);
    }

    /**
     * Builds a frequency table from a text string.
     */
    public static Map<Character, Integer> frequencies(String text) {
        Map<Character, Integer> freq = new LinkedHashMap<>();
        for (char c : text.toCharArray()) freq.merge(c, 1, Integer::sum);
        return freq;
    }

    private static void buildCodes(Node node, String prefix, Map<Character, String> codes) {
        if (node.isLeaf()) { codes.put(node.ch(), prefix.isEmpty() ? "0" : prefix); return; }
        buildCodes(node.left(),  prefix + "0", codes);
        buildCodes(node.right(), prefix + "1", codes);
    }
}
