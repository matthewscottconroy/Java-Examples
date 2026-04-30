package com.algorithms.greedy.huffman;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HuffmanTest {

    @Test
    @DisplayName("encode then decode is a perfect round-trip")
    void roundTrip() {
        String text = "hello world";
        var table = HuffmanTree.build(HuffmanTree.frequencies(text));
        assertEquals(text, table.decode(table.encode(text)));
    }

    @Test
    @DisplayName("more frequent character gets shorter code")
    void frequentCharacterShorterCode() {
        // 'a' appears 8 times, 'b' appears 2 times
        var table = HuffmanTree.build(HuffmanTree.frequencies("aaaaaaaabb"));
        String codeA = table.codeFor('a');
        String codeB = table.codeFor('b');
        assertNotNull(codeA);
        assertNotNull(codeB);
        assertTrue(codeA.length() <= codeB.length(),
            "More frequent 'a' should have code ≤ length of 'b'");
    }

    @Test
    @DisplayName("codes are prefix-free (no code is a prefix of another)")
    void prefixFree() {
        var table = HuffmanTree.build(HuffmanTree.frequencies("aabbbcccc"));
        var codes = table.allCodes().values().stream().toList();
        for (int i = 0; i < codes.size(); i++) {
            for (int j = 0; j < codes.size(); j++) {
                if (i != j) {
                    assertFalse(codes.get(j).startsWith(codes.get(i)),
                        codes.get(i) + " is a prefix of " + codes.get(j));
                }
            }
        }
    }

    @Test
    @DisplayName("single character text encodes and decodes correctly")
    void singleCharacter() {
        var table = HuffmanTree.build(Map.of('x', 5));
        String encoded = table.encode("xxxxx");
        assertEquals("xxxxx", table.decode(encoded));
    }

    @Test
    @DisplayName("compressed bit length is less than ASCII for typical text")
    void compressionRatio() {
        String text = "the quick brown fox jumps over the lazy dog";
        var table = HuffmanTree.build(HuffmanTree.frequencies(text));
        String encoded = table.encode(text);
        assertTrue(encoded.length() < text.length() * 8,
            "Huffman should compress better than ASCII");
    }

    @Test
    @DisplayName("round-trip on longer text with all ASCII printable chars")
    void roundTripLonger() {
        String text = "data compression reduces storage costs significantly!";
        var table = HuffmanTree.build(HuffmanTree.frequencies(text));
        assertEquals(text, table.decode(table.encode(text)));
    }

    @Test
    @DisplayName("frequencies counts characters correctly")
    void frequencyCounts() {
        Map<Character, Integer> freq = HuffmanTree.frequencies("aabbc");
        assertEquals(2, freq.get('a'));
        assertEquals(2, freq.get('b'));
        assertEquals(1, freq.get('c'));
    }
}
