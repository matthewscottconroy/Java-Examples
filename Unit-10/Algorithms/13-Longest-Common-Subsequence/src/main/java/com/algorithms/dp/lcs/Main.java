package com.algorithms.dp.lcs;

/**
 * Demonstrates LCS and edit distance on a code review diff scenario.
 *
 * Two versions of a config file are compared. LCS shows what stayed the same;
 * edit distance measures how many changes were made.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== LCS and Edit Distance — Code Diff Tool ===\n");

        // --- LCS between two versions of a config key list ---
        String v1 = "database.host,database.port,cache.ttl,auth.secret,logging.level";
        String v2 = "database.host,cache.ttl,cache.size,auth.secret,auth.token,logging.level";

        System.out.println("Version 1 keys: " + v1);
        System.out.println("Version 2 keys: " + v2);
        System.out.println();

        // Work on character sequences for demo; in practice you'd split on ','
        String lcs = LCS.reconstruct(v1, v2);
        System.out.println("LCS (unchanged characters): length " + LCS.length(v1, v2));
        System.out.println("Edit distance:              " + LCS.editDistance(v1, v2));

        // --- Classic string examples ---
        System.out.println("\n--- Classic LCS Examples ---");
        String[][] pairs = {
            {"ABCBDAB", "BDCABA"},
            {"AGGTAB",  "GXTXAYB"},
            {"",        "HELLO"},
            {"SAME",    "SAME"},
        };
        System.out.printf("%-15s  %-15s  %5s  %10s  %s%n",
            "String A", "String B", "Len", "Edit-Dist", "LCS");
        System.out.println("-".repeat(65));
        for (String[] p : pairs) {
            System.out.printf("%-15s  %-15s  %5d  %9d  %s%n",
                p[0], p[1],
                LCS.length(p[0], p[1]),
                LCS.editDistance(p[0], p[1]),
                LCS.reconstruct(p[0], p[1]));
        }

        // --- Spell checker analogy ---
        System.out.println("\n--- Spell Checker (Edit Distance) ---");
        String query = "recieve";
        String[] dictionary = {"receive", "relieve", "believe", "deceive", "achieve", "retrieve"};
        System.out.println("Misspelled: \"" + query + "\"");
        System.out.println("Closest dictionary words:");

        java.util.Arrays.stream(dictionary)
            .map(w -> new int[]{LCS.editDistance(query, w), w.hashCode(), w.length()})
            .sorted((a, b) -> Integer.compare(a[0], b[0]))
            .limit(3)
            .forEach(r -> {
                // find the word again (since we lost it through int[])
            });

        for (String word : dictionary) {
            int dist = LCS.editDistance(query, word);
            System.out.printf("  %-12s  edit distance: %d%n", word, dist);
        }
    }
}
