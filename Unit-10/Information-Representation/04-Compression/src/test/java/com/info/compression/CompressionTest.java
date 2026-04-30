package com.info.compression;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompressionTest {

    // -- RLE --

    @Test @DisplayName("RLE encode/decode round-trip")
    void rle_roundTrip() {
        String original = "AAABBBCCCCDDDDDD";
        assertEquals(original, RunLengthEncoder.decode(RunLengthEncoder.encode(original)));
    }

    @Test @DisplayName("RLE encodes all-same characters as count+char")
    void rle_allSame() {
        assertEquals("5A", RunLengthEncoder.encode("AAAAA"));
    }

    @Test @DisplayName("RLE encodes no-repetition with count 1")
    void rle_noRepetition() {
        assertEquals("1A1B1C", RunLengthEncoder.encode("ABC"));
    }

    @Test @DisplayName("RLE compresses highly repetitive input")
    void rle_compressionGain() {
        String s = "A".repeat(100);
        assertTrue(RunLengthEncoder.encode(s).length() < s.length());
    }

    @Test @DisplayName("RLE round-trip for empty string")
    void rle_empty() {
        assertEquals("", RunLengthEncoder.decode(RunLengthEncoder.encode("")));
    }

    @Test @DisplayName("RLE handles runs longer than 9 (multi-digit count)")
    void rle_multiDigitCount() {
        String s = "A".repeat(15);
        assertEquals("15A", RunLengthEncoder.encode(s));
        assertEquals(s, RunLengthEncoder.decode("15A"));
    }

    // -- Huffman --

    @Test @DisplayName("Huffman round-trip: compress then decompress equals original")
    void huffman_roundTrip() {
        String text = "abracadabra";
        HuffmanCoder.CompressedData cd = HuffmanCoder.compress(text);
        assertEquals(text, HuffmanCoder.decompress(cd));
    }

    @Test @DisplayName("Huffman round-trip for all-same character input")
    void huffman_singleChar() {
        String text = "AAAAAAA";
        assertEquals(text, HuffmanCoder.decompress(HuffmanCoder.compress(text)));
    }

    @Test @DisplayName("Huffman round-trip for two-character input")
    void huffman_twoChars() {
        String text = "ababababab";
        assertEquals(text, HuffmanCoder.decompress(HuffmanCoder.compress(text)));
    }

    @Test @DisplayName("Huffman round-trip for longer text")
    void huffman_longerText() {
        String text = "the quick brown fox jumps over the lazy dog";
        assertEquals(text, HuffmanCoder.decompress(HuffmanCoder.compress(text)));
    }

    @Test @DisplayName("Huffman round-trip for empty string")
    void huffman_empty() {
        assertEquals("", HuffmanCoder.decompress(HuffmanCoder.compress("")));
    }

    @Test @DisplayName("Huffman compression ratio > 1 for high-repetition input")
    void huffman_compresses() {
        String text = "aaaaaaaabbbbccdd";
        HuffmanCoder.CompressedData cd = HuffmanCoder.compress(text);
        assertTrue(cd.compressionRatio() > 1.0,
            "Expected compression ratio > 1, got " + cd.compressionRatio());
    }

    @Test @DisplayName("Huffman frequent symbol gets shorter code than rare symbol")
    void huffman_frequentSymbolShorterCode() {
        // 'a' appears 8 times, 'b' appears once — 'a' should have shorter code
        String text = "aaaaaaaa" + "b";
        HuffmanCoder.CompressedData cd = HuffmanCoder.compress(text);
        String codeA = cd.codeTable().get('a');
        String codeB = cd.codeTable().get('b');
        assertTrue(codeA.length() <= codeB.length(),
            "Expected 'a' code ≤ 'b' code length: " + codeA + " vs " + codeB);
    }

    @Test @DisplayName("Huffman code table has one entry per distinct character")
    void huffman_codeTableSize() {
        String text = "abcabc";
        HuffmanCoder.CompressedData cd = HuffmanCoder.compress(text);
        assertEquals(3, cd.codeTable().size());
    }
}
