package com.functional.methodrefs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MethodReferencesTest {

    private static final List<String> RAW = List.of(
            "Alice,Chen,alice@example.com,555-0101",
            "Bob,Patel,bob@example.com,",
            "Carol,James,carol@example.com,555-0303"
    );

    @Test
    @DisplayName("Static method reference parses CSV into Contact")
    void staticMethodRef() {
        List<Contact> contacts = RAW.stream().map(Contact::parse).collect(Collectors.toList());
        assertEquals(3, contacts.size());
        assertEquals("Alice", contacts.get(0).firstName());
    }

    @Test
    @DisplayName("Unbound instance method reference extracts full name")
    void unboundInstanceRef() {
        Function<Contact, String> fullName = Contact::fullName;
        Contact c = new Contact("Alice", "Chen", "a@b.com", "555");
        assertEquals("Alice Chen", fullName.apply(c));
    }

    @Test
    @DisplayName("Method reference as predicate — Contact::hasPhone")
    void predicateMethodRef() {
        Predicate<Contact> hasPhone = Contact::hasPhone;
        Contact withPhone    = new Contact("A", "B", "a@b.com", "555");
        Contact withoutPhone = new Contact("C", "D", "c@d.com", "");
        assertTrue(hasPhone.test(withPhone));
        assertFalse(hasPhone.test(withoutPhone));
    }

    @Test
    @DisplayName("Comparator.comparing with method reference sorts by last name")
    void comparatorMethodRef() {
        List<Contact> contacts = RAW.stream().map(Contact::parse).collect(Collectors.toList());
        List<String> sorted = contacts.stream()
                .sorted(Comparator.comparing(Contact::lastName))
                .map(Contact::lastName)
                .collect(Collectors.toList());
        assertEquals(List.of("Chen", "James", "Patel"), sorted);
    }

    @Test
    @DisplayName("Method reference and equivalent lambda are behaviourally identical")
    void methodRefEquivalentToLambda() {
        Contact c = new Contact("Alice", "Chen", "a@b.com", "555");
        Function<Contact, String> ref    = Contact::fullName;
        Function<Contact, String> lambda = contact -> contact.fullName();
        assertEquals(lambda.apply(c), ref.apply(c));
    }
}
