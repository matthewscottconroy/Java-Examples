package com.patterns.di;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DependencyInjectionTest {

    private InMemoryMailSender mailer;
    private UserRegistrationService service;

    @BeforeEach
    void setUp() {
        mailer  = new InMemoryMailSender();
        service = new UserRegistrationService(mailer);
    }

    @Test
    @DisplayName("Registering a user sends exactly one welcome email")
    void registerSendsOneEmail() {
        service.register("Alice", "alice@example.com");
        assertEquals(1, mailer.getSentMessages().size());
    }

    @Test
    @DisplayName("Welcome email is sent to the correct address")
    void emailSentToCorrectAddress() {
        service.register("Alice", "alice@example.com");
        assertEquals("alice@example.com", mailer.getSentMessages().get(0).to());
    }

    @Test
    @DisplayName("Welcome email subject contains the user's name")
    void emailSubjectContainsUsername() {
        service.register("Bob", "bob@example.com");
        assertTrue(mailer.getSentMessages().get(0).subject().contains("Bob"));
    }

    @Test
    @DisplayName("Registering multiple users sends one email each")
    void multipleRegistrationsSendMultipleEmails() {
        service.register("Alice", "alice@example.com");
        service.register("Bob",   "bob@example.com");
        service.register("Carol", "carol@example.com");
        assertEquals(3, mailer.getSentMessages().size());
    }

    @Test
    @DisplayName("Registered users are tracked in the service")
    void registeredUsersTracked() {
        service.register("Alice", "alice@example.com");
        service.register("Bob",   "bob@example.com");
        assertEquals(2, service.getRegisteredUsers().size());
        assertTrue(service.getRegisteredUsers().contains("Alice"));
    }

    @Test
    @DisplayName("Swapping the injected dependency changes behaviour without touching the service")
    void swappingDependencyChangesEmailBehaviour() {
        InMemoryMailSender altMailer = new InMemoryMailSender();
        UserRegistrationService altService = new UserRegistrationService(altMailer);

        altService.register("Dave", "dave@example.com");

        assertEquals(0, mailer.getSentMessages().size(), "Original mailer should be untouched");
        assertEquals(1, altMailer.getSentMessages().size());
    }
}
