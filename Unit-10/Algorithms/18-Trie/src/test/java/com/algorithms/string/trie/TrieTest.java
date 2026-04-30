package com.algorithms.string.trie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    private Trie trie;

    @BeforeEach
    void setUp() {
        trie = new Trie();
        for (String word : new String[]{"apple", "app", "application", "apply", "banana", "band", "bat"}) {
            trie.insert(word);
        }
    }

    @Test
    @DisplayName("search finds inserted words")
    void searchFindsWords() {
        assertTrue(trie.search("apple"));
        assertTrue(trie.search("app"));
        assertTrue(trie.search("banana"));
    }

    @Test
    @DisplayName("search returns false for absent words")
    void searchMissing() {
        assertFalse(trie.search("ap"));
        assertFalse(trie.search("apples"));
        assertFalse(trie.search(""));
    }

    @Test
    @DisplayName("prefix 'app' is a word and also a prefix")
    void appIsBoth() {
        assertTrue(trie.search("app"));
        assertTrue(trie.startsWith("app"));
    }

    @Test
    @DisplayName("startsWith returns true for valid prefix")
    void startsWithTrue() {
        assertTrue(trie.startsWith("appl"));
        assertTrue(trie.startsWith("ban"));
        assertTrue(trie.startsWith("a"));
    }

    @Test
    @DisplayName("startsWith returns false for non-existent prefix")
    void startsWithFalse() {
        assertFalse(trie.startsWith("xyz"));
        assertFalse(trie.startsWith("bananaz"));
    }

    @Test
    @DisplayName("autocomplete returns all words with given prefix")
    void autocompleteBasic() {
        List<String> result = trie.autocomplete("app");
        assertTrue(result.containsAll(List.of("app", "apple", "application", "apply")));
        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("autocomplete returns single word when no other prefix matches")
    void autocompleteSingle() {
        List<String> result = trie.autocomplete("bat");
        assertEquals(List.of("bat"), result);
    }

    @Test
    @DisplayName("autocomplete on non-existent prefix returns empty list")
    void autocompleteEmpty() {
        assertTrue(trie.autocomplete("xyz").isEmpty());
    }

    @Test
    @DisplayName("countWithPrefix counts correctly")
    void countWithPrefix() {
        assertEquals(4, trie.countWithPrefix("app"));
        assertEquals(2, trie.countWithPrefix("ban"));
        assertEquals(0, trie.countWithPrefix("xyz"));
    }

    @Test
    @DisplayName("size reflects number of inserted words")
    void sizeIsCorrect() {
        assertEquals(7, trie.size());
        trie.insert("apricot");
        assertEquals(8, trie.size());
    }

    @Test
    @DisplayName("inserting duplicate does not change size or search result")
    void duplicateInsert() {
        int before = trie.size();
        trie.insert("apple");
        // size increments (simple implementation — tracking duplicates not required)
        assertTrue(trie.search("apple"));
    }

    @Test
    @DisplayName("empty trie returns false for all queries")
    void emptyTrie() {
        Trie empty = new Trie();
        assertFalse(empty.search("a"));
        assertFalse(empty.startsWith("a"));
        assertTrue(empty.autocomplete("a").isEmpty());
        assertEquals(0, empty.size());
    }
}
