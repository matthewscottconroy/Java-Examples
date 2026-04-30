package com.info.theory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InformationTheoryTest {

    private static final double DELTA = 1e-6;

    // -- Self-information --

    @Test @DisplayName("I(P=1) = 0 bits (certain event carries no information)")
    void self_certainty() {
        assertEquals(0.0, InformationTheory.selfInformation(1.0), DELTA);
    }

    @Test @DisplayName("I(P=0.5) = 1 bit (fair coin)")
    void self_halfProbability() {
        assertEquals(1.0, InformationTheory.selfInformation(0.5), DELTA);
    }

    @Test @DisplayName("I(P=0.25) = 2 bits")
    void self_quarterProbability() {
        assertEquals(2.0, InformationTheory.selfInformation(0.25), DELTA);
    }

    @Test @DisplayName("I(P) increases as probability decreases")
    void self_monotoneDecreasing() {
        assertTrue(InformationTheory.selfInformation(0.1) > InformationTheory.selfInformation(0.5));
    }

    @Test @DisplayName("selfInformation throws for P=0")
    void self_zeroProbability() {
        assertThrows(IllegalArgumentException.class, () -> InformationTheory.selfInformation(0));
    }

    // -- Shannon entropy --

    @Test @DisplayName("Entropy of constant string is 0")
    void entropy_constant() {
        assertEquals(0.0, InformationTheory.shannonEntropy("aaaaaaa"), DELTA);
    }

    @Test @DisplayName("Entropy of empty string is 0")
    void entropy_empty() {
        assertEquals(0.0, InformationTheory.shannonEntropy(""), DELTA);
    }

    @Test @DisplayName("Entropy of 'ab' (equal probability) = 1 bit")
    void entropy_equalBinary() {
        assertEquals(1.0, InformationTheory.shannonEntropy("ab"), DELTA);
    }

    @Test @DisplayName("Entropy of 4 equally likely symbols = 2 bits")
    void entropy_fourSymbols() {
        assertEquals(2.0, InformationTheory.shannonEntropy("abcd"), DELTA);
    }

    @Test @DisplayName("Entropy is at most log₂(distinct symbols)")
    void entropy_boundedByAlphabet() {
        String text = "hello world";
        long k = text.chars().distinct().count();
        assertTrue(InformationTheory.shannonEntropy(text) <= InformationTheory.maxEntropy((int) k) + DELTA);
    }

    @Test @DisplayName("Higher repetition → lower entropy")
    void entropy_repetitionLowers() {
        // "aabba" has lower entropy than "abcde"
        double h1 = InformationTheory.shannonEntropy("aabba");
        double h2 = InformationTheory.shannonEntropy("abcde");
        assertTrue(h1 < h2, "aabba should have lower entropy than abcde");
    }

    // -- Max entropy / redundancy --

    @Test @DisplayName("maxEntropy(2) = 1 bit")
    void maxEntropy_binary() {
        assertEquals(1.0, InformationTheory.maxEntropy(2), DELTA);
    }

    @Test @DisplayName("maxEntropy(256) = 8 bits")
    void maxEntropy_byte() {
        assertEquals(8.0, InformationTheory.maxEntropy(256), DELTA);
    }

    @Test @DisplayName("Redundancy of constant string = 1")
    void redundancy_constant() {
        assertEquals(1.0, InformationTheory.redundancy("aaaa"), DELTA);
    }

    @Test @DisplayName("Redundancy of uniform distribution = 0")
    void redundancy_uniform() {
        assertEquals(0.0, InformationTheory.redundancy("abcd"), DELTA);
    }

    // -- Compression ratio --

    @Test @DisplayName("Highly repetitive string compresses well (ratio > 1)")
    void compression_repetitive() throws Exception {
        assertTrue(InformationTheory.compressionRatio("a".repeat(1000)) > 1.0);
    }

    @Test @DisplayName("gzipSize returns positive integer")
    void gzip_positive() throws Exception {
        assertTrue(InformationTheory.gzipSize("hello") > 0);
    }

    // -- Mutual information --

    @Test @DisplayName("MI(x, x) = H(x) — maximum mutual information")
    void mi_selfInformation() {
        String x = "abcabc";
        assertEquals(
            InformationTheory.shannonEntropy(x),
            InformationTheory.mutualInformation(x, x),
            DELTA);
    }

    @Test @DisplayName("MI is non-negative")
    void mi_nonNegative() {
        String x = "abcabcabc";
        String y = "xyzxyzxyz";
        assertTrue(InformationTheory.mutualInformation(x, y) >= -DELTA);
    }

    @Test @DisplayName("MI throws when strings have different lengths")
    void mi_differentLengths() {
        assertThrows(IllegalArgumentException.class,
            () -> InformationTheory.mutualInformation("abc", "abcd"));
    }
}
