package com.info.theory;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Information Theory — Quantifying Surprise ===\n");

        // ---------------------------------------------------------------
        // 1. Self-information
        // ---------------------------------------------------------------
        System.out.println("--- 1. Self-information I(x) = -log₂(P) ---");
        double[][] examples = {{1.0, 1}, {0.5, 2}, {0.25, 4}, {0.125, 8}, {0.01, 100}};
        for (double[] e : examples) {
            System.out.printf("  P=1/%-4.0f  I = %.4f bits%n", e[1],
                InformationTheory.selfInformation(e[0]));
        }

        // ---------------------------------------------------------------
        // 2. Shannon entropy for various strings
        // ---------------------------------------------------------------
        System.out.println("\n--- 2. Shannon entropy H(X) ---");
        String[] samples = {
            "aaaaaaaaaa",          // zero entropy
            "aabb",                // 1 bit (two equally likely symbols)
            "abcd",                // 2 bits (four equally likely)
            "hello world",
            "the quick brown fox jumps over the lazy dog",
            "4f3c8a2e9b1d7605"    // high entropy (hex digits)
        };
        for (String s : samples) {
            double H = InformationTheory.shannonEntropy(s);
            long k   = s.chars().distinct().count();
            System.out.printf("  %-46s  H=%.4f bits  max=%.4f  redundancy=%.2f%%%n",
                "\"" + (s.length() > 40 ? s.substring(0, 40) + "…" : s) + "\"",
                H, InformationTheory.maxEntropy((int) k),
                100 * InformationTheory.redundancy(s));
        }

        // ---------------------------------------------------------------
        // 3. Complexity via compression
        // ---------------------------------------------------------------
        System.out.println("\n--- 3. Complexity approximated by GZIP size ---");
        String[] texts = {
            "a".repeat(1000),
            "abcabc".repeat(167),
            "the quick brown fox ".repeat(50),
            new java.util.Random(42).ints(1000, 32, 127)
                .collect(StringBuilder::new, (sb, c) -> sb.append((char) c), StringBuilder::append)
                .toString()
        };
        String[] labels = {"1000×'a'", "abcabc×167", "sentence×50", "random ASCII"};
        for (int i = 0; i < texts.length; i++) {
            double ratio = InformationTheory.compressionRatio(texts[i]);
            System.out.printf("  %-14s  original=%5d  gzip=%4d  ratio=%.2f  H=%.2f bits%n",
                labels[i],
                texts[i].getBytes().length,
                InformationTheory.gzipSize(texts[i]),
                ratio,
                InformationTheory.shannonEntropy(texts[i]));
        }

        // ---------------------------------------------------------------
        // 4. Mutual information
        // ---------------------------------------------------------------
        System.out.println("\n--- 4. Mutual information ---");
        String a = "abcabcabc";
        String b = "abcabcabc"; // identical → MI = H(a)
        String c = "xyzxyzxyz"; // same structure, different symbols — lower MI
        String d = new java.util.Random(0).ints(a.length(), 97, 104)
            .collect(StringBuilder::new, (sb, ch) -> sb.append((char) ch), StringBuilder::append)
            .toString(); // random
        System.out.printf("  MI(a, a)          = %.4f bits%n", InformationTheory.mutualInformation(a, a));
        System.out.printf("  MI(a, shifted)    = %.4f bits%n", InformationTheory.mutualInformation(a, c));
        System.out.printf("  MI(a, random)     = %.4f bits%n", InformationTheory.mutualInformation(a, d));
    }
}
