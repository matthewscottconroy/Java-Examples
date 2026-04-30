package com.patterns.prototype;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Prototype pattern — Legal Contract Library.
 */
class ContractTest {

    @Test
    @DisplayName("Clone is not the same object as the original")
    void cloneIsDistinct() {
        Contract original = new Contract("NDA").addClause("Keep it secret.");
        Contract copy = original.clone();
        assertNotSame(original, copy);
    }

    @Test
    @DisplayName("Clone has the same type and clauses as the original")
    void cloneMatchesOriginal() {
        Contract original = new Contract("NDA")
                .addClause("Clause 1")
                .addClause("Clause 2");
        Contract copy = original.clone();
        assertEquals(original.getType(),        copy.getType());
        assertEquals(original.getClauseCount(), copy.getClauseCount());
    }

    @Test
    @DisplayName("Modifying the clone does not affect the original")
    void cloneIsIndependent() {
        Contract original = new Contract("NDA").addClause("Standard clause.");
        Contract copy = original.clone();

        copy.customise("Acme", "Beta", "2026-01-01");
        copy.addClause("Extra clause.");

        // Original party name is still the placeholder
        assertEquals("[PARTY A]", original.getPartyA());
        // Original clause count is unchanged
        assertEquals(1, original.getClauseCount());
    }

    @Test
    @DisplayName("ContractLibrary returns independent clones")
    void libraryReturnsClones() {
        ContractLibrary lib = new ContractLibrary();
        lib.register("NDA", new Contract("NDA").addClause("Confidential."));

        Contract a = lib.get("NDA").customise("A", "B", "2026-01-01");
        Contract b = lib.get("NDA").customise("C", "D", "2026-06-01");

        assertNotSame(a, b);
        assertEquals("A", a.getPartyA());
        assertEquals("C", b.getPartyA());
    }

    @Test
    @DisplayName("Unknown template key throws IllegalArgumentException")
    void unknownKeyThrows() {
        ContractLibrary lib = new ContractLibrary();
        assertThrows(IllegalArgumentException.class, () -> lib.get("MISSING"));
    }
}
