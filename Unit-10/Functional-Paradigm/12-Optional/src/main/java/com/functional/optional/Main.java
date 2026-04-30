package com.functional.optional;

import java.util.Optional;

/**
 * Demonstrates Optional with a user profile lookup chain.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== User Profile Lookup (Optional) ===\n");

        UserRepository repo = new UserRepository();

        // isPresent / get — the basics (but usually better to use map/orElse)
        Optional<UserProfile> aliceOpt = repo.findById("u001");
        System.out.println("Found alice: " + aliceOpt.isPresent());

        // orElse — provide a default if empty
        String display = repo.findById("u001")
                .map(UserProfile::displayName)
                .orElse("Anonymous");
        System.out.println("Alice's display name: " + display);

        // Unknown user — orElse kicks in
        String unknown = repo.findById("u999")
                .map(UserProfile::displayName)
                .orElse("Anonymous");
        System.out.println("Unknown user display: " + unknown);

        // orElseGet — supplier called lazily (avoids cost when present)
        String tier = repo.findById("u001")
                .map(UserProfile::tier)
                .orElseGet(() -> "free");
        System.out.println("Alice's tier: " + tier);

        // Chained map — drill into a nullable field safely
        // Bob's email is null; Optional.ofNullable wraps the inner null
        String bobEmail = repo.findById("u002")
                .map(UserProfile::email)
                .map(String::toUpperCase)
                .orElse("(no verified email)");
        System.out.println("Bob's email: " + bobEmail);

        // filter — keep value only if it satisfies a predicate
        boolean isEnterprise = repo.findById("u003")
                .filter(u -> u.tier().equals("enterprise"))
                .isPresent();
        System.out.println("Carol is enterprise: " + isEnterprise);

        // orElseThrow — signal an error when absence is truly exceptional
        try {
            UserProfile must = repo.findById("u999")
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } catch (IllegalArgumentException e) {
            System.out.println("Expected: " + e.getMessage());
        }

        // ifPresent — side-effect only if value exists
        repo.findById("u001")
                .ifPresent(u -> System.out.println("Sending welcome email to: " + u.email()));
    }
}
