package com.algorithms.string.trie;

import java.util.List;

/**
 * Demonstrates trie usage in a search autocomplete scenario.
 *
 * A search engine needs to suggest completions as a user types. A trie answers
 * "all words starting with this prefix" efficiently — O(prefix length + results).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Trie — Search Autocomplete ===\n");

        Trie trie = new Trie();

        // Load a dictionary of programming terms
        String[] terms = {
            "algorithm", "allocation", "array", "abstract",
            "binary", "boolean", "buffer", "bytecode",
            "class", "cache", "callback", "closure", "compiler",
            "data", "database", "deadlock", "debug", "decorator",
            "exception", "encapsulation", "enum",
            "function", "factory", "fibonacci",
            "garbage", "generics", "graph",
            "hash", "heap", "hook",
            "interface", "inheritance", "iterator",
            "lambda", "latency", "linked",
            "memory", "mutex", "method",
            "null", "node",
            "object", "observer", "overflow",
            "pattern", "pointer", "polymorphism", "proxy",
            "queue", "quicksort",
            "recursion", "refactoring", "runtime",
            "singleton", "stack", "stream", "symbol",
            "thread", "tree", "type",
            "union", "unit"
        };

        for (String term : terms) trie.insert(term);
        System.out.println("Dictionary loaded: " + trie.size() + " terms\n");

        // Autocomplete queries
        String[] queries = {"al", "c", "re", "s", "th"};
        for (String query : queries) {
            List<String> suggestions = trie.autocomplete(query);
            System.out.printf("Query \"%s\" → %d suggestions: %s%n",
                query, suggestions.size(), suggestions);
        }

        // Exact lookup
        System.out.println();
        System.out.println("search(\"recursion\"):  " + trie.search("recursion"));
        System.out.println("search(\"recurse\"):    " + trie.search("recurse"));
        System.out.println("startsWith(\"rec\"):    " + trie.startsWith("rec"));
        System.out.println("countWithPrefix(\"s\"): " + trie.countWithPrefix("s"));

        // Insert and immediate lookup
        trie.insert("recurse");
        System.out.println("\nAfter inserting \"recurse\":");
        System.out.println("search(\"recurse\"): " + trie.search("recurse"));
        System.out.println("autocomplete(\"rec\"): " + trie.autocomplete("rec"));
    }
}
