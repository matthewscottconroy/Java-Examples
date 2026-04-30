package com.patterns.bridge;

/**
 * Concrete Implementor — delivers messages via email.
 */
public class EmailChannel implements MessageChannel {

    @Override
    public void send(String recipient, String subject, String body) {
        System.out.printf("[EMAIL → %s]%n  Subject: %s%n  Body: %s%n",
                recipient, subject, body);
    }

    @Override
    public String channelName() { return "Email"; }
}
