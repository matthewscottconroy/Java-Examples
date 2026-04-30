package com.patterns.interpreter;

import java.util.*;

/**
 * The context — an inverted index mapping each term to the set of documents
 * that contain it.
 */
public class InvertedIndex {

    private final Map<String, Set<String>> index = new HashMap<>();
    private final Set<String> allDocIds = new HashSet<>();

    /** Index a document with the given terms. */
    public void addDocument(String docId, String... terms) {
        allDocIds.add(docId);
        for (String term : terms) {
            index.computeIfAbsent(term.toLowerCase(), k -> new HashSet<>()).add(docId);
        }
    }

    /** Return all document IDs containing the exact term. */
    public Set<String> lookup(String term) {
        return Collections.unmodifiableSet(
                index.getOrDefault(term.toLowerCase(), Set.of()));
    }

    /** Return every document ID in the index. */
    public Set<String> allDocuments() {
        return Collections.unmodifiableSet(allDocIds);
    }
}
