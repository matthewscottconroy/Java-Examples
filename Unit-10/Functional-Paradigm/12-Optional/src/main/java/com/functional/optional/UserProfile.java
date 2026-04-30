package com.functional.optional;

/**
 * Immutable user profile.
 *
 * @param userId      unique identifier
 * @param displayName user's chosen display name
 * @param email       verified email (null if not verified)
 * @param tier        subscription tier: "free", "pro", or "enterprise"
 */
public record UserProfile(String userId, String displayName, String email, String tier) {}
