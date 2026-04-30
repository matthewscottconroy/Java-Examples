package com.patterns.bridge;

/**
 * Demonstrates the Bridge pattern with notifications and delivery channels.
 *
 * <p>Two notification types (UrgentAlert, Newsletter) cross two delivery
 * channels (Email, SMS) — four combinations, zero code duplication.
 * Switching a notification to a different channel is a one-liner.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Notification Bridge (Bridge Pattern) ===\n");

        MessageChannel email = new EmailChannel();
        MessageChannel sms   = new SmsChannel();

        // Urgent alert via email
        System.out.println("-- Urgent alert via Email --");
        new UrgentAlert(email, "Server CPU at 99% — immediate action required!")
                .send("ops-team@company.com");

        System.out.println();

        // Same alert, different channel — change the bridge, not the abstraction
        System.out.println("-- Same alert via SMS --");
        new UrgentAlert(sms, "Server CPU at 99% — immediate action required!")
                .send("+1-555-0100");

        System.out.println();

        // Newsletter via email
        System.out.println("-- Newsletter via Email --");
        new Newsletter(email, "April Product Digest",
                "This month: new dashboard features, performance improvements, and the upcoming roadmap Q3.")
                .send("subscribers@company.com");

        System.out.println();

        // Newsletter via SMS — the channel truncates automatically
        System.out.println("-- Newsletter via SMS (truncated by channel) --");
        new Newsletter(sms, "April Product Digest",
                "This month: new dashboard features, performance improvements, and the upcoming roadmap Q3.")
                .send("+1-555-0200");
    }
}
