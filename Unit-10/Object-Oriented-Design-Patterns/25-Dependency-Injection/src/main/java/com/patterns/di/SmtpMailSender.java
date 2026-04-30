package com.patterns.di;

/**
 * Production implementation — sends email via SMTP.
 *
 * <p>In this demo the SMTP call is simulated; in a real application it would
 * use JavaMail or a cloud email API.
 */
public class SmtpMailSender implements MailSender {

    private final String smtpHost;
    private final int    smtpPort;

    public SmtpMailSender(String smtpHost, int smtpPort) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
    }

    @Override
    public void send(String to, String subject, String body) {
        System.out.printf("[SMTP %s:%d] → %s | %s%n", smtpHost, smtpPort, to, subject);
    }
}
