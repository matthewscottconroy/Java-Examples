package com.patterns.di;

/**
 * Demonstrates Dependency Injection with a user registration service.
 *
 * <p>The composition root (this class) is the only place that decides
 * which {@link MailSender} implementation to use.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== User Registration Service (Dependency Injection) ===\n");

        // Production: inject the real SMTP sender
        MailSender smtpSender = new SmtpMailSender("mail.example.com", 587);
        UserRegistrationService prodService = new UserRegistrationService(smtpSender);
        prodService.register("Alice Chen",  "alice@example.com");
        prodService.register("Bob Patel",   "bob@example.com");

        System.out.println();

        // Development: inject an in-memory sender — no real emails sent
        InMemoryMailSender devMailer = new InMemoryMailSender();
        UserRegistrationService devService = new UserRegistrationService(devMailer);
        devService.register("Carol James", "carol@example.com");

        System.out.println("\nMessages captured by in-memory sender:");
        devMailer.getSentMessages()
                 .forEach(m -> System.out.println("  → " + m.to() + ": " + m.subject()));
    }
}
