package com.info.theory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * Shannon information theory — quantifying the amount of information in data.
 *
 * <p>Claude Shannon (1948) defined information as the reduction of uncertainty.
 * A message you already knew carries no information; a surprising message
 * carries a lot. These concepts underpin every modern compression and
 * communication standard.
 *
 * <p>Key quantities:
 * <ul>
 *   <li><b>Self-information</b> {@code I(x) = -log₂ P(x)} bits — information
 *       content of a single outcome.
 *   <li><b>Shannon entropy</b> {@code H(X) = Σ -P(x) log₂ P(x)} — expected
 *       information per symbol; average bits needed to represent the source.
 *   <li><b>Maximum entropy</b> — achieved when all symbols are equally likely
 *       ({@code log₂ k} bits for an alphabet of {@code k} symbols).
 *   <li><b>Redundancy</b> — how far a source is below its maximum entropy;
 *       the part that compression can exploit.
 * </ul>
 */
public final class InformationTheory {

    private InformationTheory() {}

    // ---------------------------------------------------------------
    // Self-information and entropy
    // ---------------------------------------------------------------

    /**
     * Self-information of a single outcome with the given probability.
     * A certain event (P=1) carries 0 bits; a rare event (P→0) carries many bits.
     */
    public static double selfInformation(double probability) {
        if (probability <= 0) throw new IllegalArgumentException("Probability must be > 0");
        if (probability > 1)  throw new IllegalArgumentException("Probability must be ≤ 1");
        return -log2(probability);
    }

    /**
     * Shannon entropy of the character distribution in {@code text}, in bits per symbol.
     * Measures how unpredictable (information-dense) the source is.
     *
     * <p>0 bits → all characters identical (perfectly predictable).
     * log₂(k) bits → all k characters equally likely (maximally surprising).
     */
    public static double shannonEntropy(String text) {
        if (text.isEmpty()) return 0.0;
        Map<Integer, Long> freq = text.chars()
            .boxed()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        double n = text.length();
        return freq.values().stream()
            .mapToDouble(count -> {
                double p = count / n;
                return -p * log2(p);
            })
            .sum();
    }

    /**
     * Maximum possible entropy for an alphabet of {@code k} distinct symbols.
     * Equals {@code log₂(k)} bits — achieved when all symbols are equally probable.
     */
    public static double maxEntropy(int alphabetSize) {
        return log2(alphabetSize);
    }

    /**
     * Redundancy: fraction of the maximum entropy that is unused.
     * 0 = maximally compressed source; 1 = completely predictable.
     */
    public static double redundancy(String text) {
        long distinct = text.chars().distinct().count();
        if (distinct <= 1) return 1.0;
        double h = shannonEntropy(text);
        double hMax = maxEntropy((int) distinct);
        return 1.0 - h / hMax;
    }

    // ---------------------------------------------------------------
    // Complexity via compression
    // ---------------------------------------------------------------

    /**
     * Approximates Kolmogorov complexity by GZIP size: the length of the
     * shortest program (compressed description) that produces the string.
     *
     * <p>Low GZIP size → the string has a short description (low complexity;
     * e.g. {@code "aaa…"} is just "1000 a's").
     * High GZIP size → the string is hard to describe compactly (high complexity;
     * e.g. a truly random string cannot be compressed).
     */
    public static int gzipSize(String text) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(baos)) {
            gz.write(text.getBytes(StandardCharsets.UTF_8));
        }
        return baos.size();
    }

    public static double compressionRatio(String text) throws IOException {
        int original   = text.getBytes(StandardCharsets.UTF_8).length;
        int compressed = gzipSize(text);
        return (double) original / compressed;
    }

    // ---------------------------------------------------------------
    // Mutual information (bivariate)
    // ---------------------------------------------------------------

    /**
     * Measures how much knowing X reduces uncertainty about Y (and vice versa).
     * For two strings of equal length, treats each index as a paired observation.
     * Returns 0 if the strings are independent; higher values → more shared structure.
     */
    public static double mutualInformation(String x, String y) {
        if (x.length() != y.length())
            throw new IllegalArgumentException("Strings must have equal length");
        if (x.isEmpty()) return 0.0;
        return shannonEntropy(x) + shannonEntropy(y) - jointEntropy(x, y);
    }

    /** Joint entropy H(X, Y) over character pairs at each position. */
    public static double jointEntropy(String x, String y) {
        if (x.length() != y.length())
            throw new IllegalArgumentException("Strings must have equal length");
        if (x.isEmpty()) return 0.0;
        Map<String, Long> pairFreq = new java.util.HashMap<>();
        for (int i = 0; i < x.length(); i++) {
            String pair = x.charAt(i) + "," + y.charAt(i);
            pairFreq.merge(pair, 1L, Long::sum);
        }
        double n = x.length();
        return pairFreq.values().stream()
            .mapToDouble(count -> { double p = count / n; return -p * log2(p); })
            .sum();
    }

    // ---------------------------------------------------------------
    // Utility
    // ---------------------------------------------------------------

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}
