package com.patterns.di;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service that registers users and sends them a welcome email.
 *
 * <p>The {@link MailSender} is injected via the constructor, so this class
 * never decides which email implementation to use. Changing from SMTP to
 * a cloud API requires no change here.
 */
public class UserRegistrationService {

    private final MailSender mailer;
    private final List<String> registeredUsers = new ArrayList<>();

    /** Constructor injection — the only way dependencies should arrive. */
    public UserRegistrationService(MailSender mailer) {
        this.mailer = mailer;
    }

    /**
     * Register a user and send a welcome email.
     *
     * @param username display name
     * @param email    email address
     */
    public void register(String username, String email) {
        registeredUsers.add(username);
        mailer.send(
                email,
                "Welcome to the platform, " + username + "!",
                "Hi " + username + ",\n\nThanks for signing up. We're glad to have you.\n\nThe Team");
        System.out.println("[Service] Registered user: " + username);
    }

    public List<String> getRegisteredUsers() {
        return Collections.unmodifiableList(registeredUsers);
    }
}
