package com.patterns.di;

/**
 * Abstraction for sending email — the dependency that gets injected.
 *
 * <p>Production code injects {@link SmtpMailSender}; tests inject
 * {@link InMemoryMailSender} to avoid real network calls.
 */
public interface MailSender {

    /**
     * Send an email message.
     *
     * @param to      recipient address
     * @param subject email subject line
     * @param body    plain-text body
     */
    void send(String to, String subject, String body);
}
