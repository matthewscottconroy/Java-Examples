package com.functional.optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class OptionalTest {

    private UserRepository repo;

    @BeforeEach
    void setUp() { repo = new UserRepository(); }

    @Test
    @DisplayName("findById returns present Optional for a known user")
    void findKnownUser() {
        Optional<UserProfile> result = repo.findById("u001");
        assertTrue(result.isPresent());
        assertEquals("Alice Chen", result.get().displayName());
    }

    @Test
    @DisplayName("findById returns empty Optional for an unknown user")
    void findUnknownUser() {
        assertTrue(repo.findById("u999").isEmpty());
    }

    @Test
    @DisplayName("orElse provides a fallback for absent value")
    void orElseFallback() {
        String name = repo.findById("u999")
                .map(UserProfile::displayName)
                .orElse("Guest");
        assertEquals("Guest", name);
    }

    @Test
    @DisplayName("map transforms the value inside Optional")
    void mapTransforms() {
        String tier = repo.findById("u001")
                .map(UserProfile::tier)
                .orElse("free");
        assertEquals("pro", tier);
    }

    @Test
    @DisplayName("map on empty Optional stays empty")
    void mapOnEmpty() {
        Optional<String> result = repo.findById("u999").map(UserProfile::tier);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("filter keeps value when predicate matches")
    void filterMatches() {
        Optional<UserProfile> enterprise = repo.findById("u003")
                .filter(u -> u.tier().equals("enterprise"));
        assertTrue(enterprise.isPresent());
    }

    @Test
    @DisplayName("filter discards value when predicate fails")
    void filterNoMatch() {
        Optional<UserProfile> enterprise = repo.findById("u001")
                .filter(u -> u.tier().equals("enterprise"));
        assertTrue(enterprise.isEmpty());
    }

    @Test
    @DisplayName("orElseThrow throws when Optional is empty")
    void orElseThrowOnEmpty() {
        assertThrows(IllegalArgumentException.class, () ->
                repo.findById("u999").orElseThrow(() -> new IllegalArgumentException("not found")));
    }

    @Test
    @DisplayName("ifPresent is called only when value is present")
    void ifPresentOnlyWhenPresent() {
        AtomicBoolean called = new AtomicBoolean(false);
        repo.findById("u001").ifPresent(u -> called.set(true));
        assertTrue(called.get());

        called.set(false);
        repo.findById("u999").ifPresent(u -> called.set(true));
        assertFalse(called.get());
    }
}
