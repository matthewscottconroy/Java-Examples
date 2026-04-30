package com.functional.optional;

import java.util.Map;
import java.util.Optional;

/**
 * In-memory user store returning {@link Optional} instead of null.
 *
 * <p>Returning {@code Optional<UserProfile>} makes the possibility of a
 * missing user explicit in the type system. The caller cannot accidentally
 * use a missing value without acknowledging it.
 */
public class UserRepository {

    private final Map<String, UserProfile> store = Map.of(
            "u001", new UserProfile("u001", "Alice Chen",  "alice@example.com", "pro"),
            "u002", new UserProfile("u002", "Bob Patel",   null,                "free"),
            "u003", new UserProfile("u003", "Carol James", "carol@example.com", "enterprise")
    );

    /**
     * Find a user by ID.
     *
     * @param userId the user's identifier
     * @return the user profile, or {@link Optional#empty()} if not found
     */
    public Optional<UserProfile> findById(String userId) {
        return Optional.ofNullable(store.get(userId));
    }
}
