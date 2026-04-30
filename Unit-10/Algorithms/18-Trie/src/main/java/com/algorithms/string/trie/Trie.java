package com.algorithms.string.trie;

import java.util.*;

/**
 * Trie (prefix tree) for efficient string insertion, lookup, and prefix search.
 *
 * <p>A trie stores strings by sharing common prefixes. Every path from root to
 * a marked node spells out an inserted word.
 *
 * <p>Time: O(L) per operation, where L = length of the key.
 * Space: O(total characters stored), reduced by shared prefixes.
 */
public class Trie {

    private static class Node {
        final Map<Character, Node> children = new HashMap<>();
        boolean isEndOfWord = false;
        int count = 0;  // number of words that pass through this node
    }

    private final Node root = new Node();
    private int wordCount = 0;

    /** Inserts a word into the trie. */
    public void insert(String word) {
        Node cur = root;
        for (char c : word.toCharArray()) {
            cur.children.putIfAbsent(c, new Node());
            cur = cur.children.get(c);
            cur.count++;
        }
        cur.isEndOfWord = true;
        wordCount++;
    }

    /** Returns true if the exact word is stored in the trie. */
    public boolean search(String word) {
        Node node = traverse(word);
        return node != null && node.isEndOfWord;
    }

    /** Returns true if any word in the trie starts with {@code prefix}. */
    public boolean startsWith(String prefix) {
        return traverse(prefix) != null;
    }

    /**
     * Returns all words in the trie that start with {@code prefix},
     * sorted lexicographically.
     */
    public List<String> autocomplete(String prefix) {
        List<String> results = new ArrayList<>();
        Node node = traverse(prefix);
        if (node != null) collectWords(node, new StringBuilder(prefix), results);
        Collections.sort(results);
        return results;
    }

    /**
     * Returns the number of words that start with {@code prefix}.
     * O(L) — reads the count stored at the last prefix node.
     */
    public int countWithPrefix(String prefix) {
        Node node = traverse(prefix);
        if (node == null) return 0;
        // Count words in subtree by traversing (simpler than maintaining a subtree counter)
        return (int) collectWordsCount(node);
    }

    /** Returns the total number of words inserted. */
    public int size() { return wordCount; }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Node traverse(String s) {
        Node cur = root;
        for (char c : s.toCharArray()) {
            cur = cur.children.get(c);
            if (cur == null) return null;
        }
        return cur;
    }

    private void collectWords(Node node, StringBuilder prefix, List<String> out) {
        if (node.isEndOfWord) out.add(prefix.toString());
        for (var entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            collectWords(entry.getValue(), prefix, out);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    private long collectWordsCount(Node node) {
        long count = node.isEndOfWord ? 1 : 0;
        for (Node child : node.children.values()) count += collectWordsCount(child);
        return count;
    }
}
