package com.patterns.bridge;

/**
 * Concrete Implementor — delivers messages via SMS (160-char limit enforced).
 */
public class SmsChannel implements MessageChannel {

    private static final int SMS_LIMIT = 160;

    @Override
    public void send(String recipient, String subject, String body) {
        // SMS has no subject — prepend it to the body and truncate
        String full = subject + ": " + body;
        String text = full.length() > SMS_LIMIT ? full.substring(0, SMS_LIMIT - 3) + "..." : full;
        System.out.printf("[SMS → %s]%n  %s%n", recipient, text);
    }

    @Override
    public String channelName() { return "SMS"; }
}
