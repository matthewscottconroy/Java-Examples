package com.algorithms.greedy.huffman;

import java.util.Map;

/**
 * Demonstrates Huffman coding on a log message compression scenario.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Huffman Coding — Log Compression ===\n");

        String text = "the quick brown fox jumps over the lazy dog";
        System.out.println("Original text: \"" + text + "\"");
        System.out.println("Length: " + text.length() + " characters\n");

        // Build the Huffman tree
        Map<Character, Integer> freq = HuffmanTree.frequencies(text);
        HuffmanTree.CodeTable table = HuffmanTree.build(freq);

        // Print the code table
        System.out.println("Character frequencies and Huffman codes:");
        System.out.printf("  %-8s  %6s  %s%n", "Char", "Freq", "Code");
        System.out.println("  " + "-".repeat(30));

        freq.entrySet().stream()
            .sorted(Map.Entry.<Character, Integer>comparingByValue().reversed())
            .forEach(e -> {
                char c = e.getKey();
                String display = c == ' ' ? "SPACE" : String.valueOf(c);
                System.out.printf("  %-8s  %6d  %s%n",
                    display, e.getValue(), table.codeFor(c));
            });

        // Encode
        String encoded = table.encode(text);
        int originalBits = text.length() * 8;
        int compressedBits = encoded.length();
        System.out.printf("%nEncoded length:  %d bits%n", compressedBits);
        System.out.printf("Original length: %d bits (ASCII)%n", originalBits);
        System.out.printf("Compression:     %.1f%%%n",
            (1.0 - (double) compressedBits / originalBits) * 100);

        // Decode and verify round-trip
        String decoded = table.decode(encoded);
        System.out.printf("%nRound-trip OK:   %b%n", text.equals(decoded));

        // Fixed-frequency demo showing the greedy choice
        System.out.println("\n--- Greedy Choice Example ---");
        System.out.println("Text: \"aabbbcccc\" (a:2, b:3, c:4)");
        var simple = HuffmanTree.build(HuffmanTree.frequencies("aabbbcccc"));
        simple.allCodes().forEach((c, code) ->
            System.out.printf("  %c → %s%n", c, code));
    }
}
