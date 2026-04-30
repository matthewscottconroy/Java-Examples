package com.functional.methodrefs;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demonstrates all four kinds of method reference with a contact book.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Contact Book (Method References) ===\n");

        List<String> raw = List.of(
                "Alice,Chen,alice@example.com,555-0101",
                "Bob,Patel,bob@example.com,",
                "Carol,James,carol@example.com,555-0303",
                "Dave,Nguyen,dave@example.com,555-0404",
                "Eve,Torres,eve@example.com,"
        );

        // 1. Static method reference  — ClassName::staticMethod
        //    Contact::parse  ≡  csv -> Contact.parse(csv)
        List<Contact> contacts = raw.stream()
                .map(Contact::parse)
                .collect(Collectors.toList());

        System.out.println("1. Static method reference — Contact::parse");
        contacts.forEach(System.out::println); // 2. Instance method ref on a known instance (println)

        // 2. Instance method reference on an arbitrary instance — ClassName::instanceMethod
        //    Contact::fullName  ≡  c -> c.fullName()
        System.out.println("\n2. Unbound instance reference — Contact::fullName");
        List<String> names = contacts.stream()
                .map(Contact::fullName)
                .collect(Collectors.toList());
        names.forEach(System.out::println);

        // 3. Instance method reference on a particular (bound) instance — instance::method
        //    System.out::println  ≡  s -> System.out.println(s)
        System.out.println("\n3. Bound instance reference — System.out::println");
        contacts.stream()
                .map(Contact::email)
                .forEach(System.out::println);

        // 4. Constructor reference  — ClassName::new
        //    Used here via a helper to build a Contact from parts
        System.out.println("\n4. Predicate method reference — Contact::hasPhone");
        List<Contact> withPhone = contacts.stream()
                .filter(Contact::hasPhone)
                .collect(Collectors.toList());
        System.out.println("Contacts with a phone number:");
        withPhone.forEach(c -> System.out.println("  " + c.fullName() + " — " + c.phone()));

        // Sort using Comparator.comparing with method reference
        System.out.println("\n5. Comparator via method reference — Contact::lastName");
        contacts.stream()
                .sorted(Comparator.comparing(Contact::lastName))
                .forEach(c -> System.out.println("  " + c.lastName() + ", " + c.firstName()));
    }
}
