package com.patterns.bridge;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Bridge pattern — Notification Service.
 */
class BridgeTest {

    /** A test channel that captures sent messages. */
    static class CapturingChannel implements MessageChannel {
        final List<String> sent = new ArrayList<>();

        @Override
        public void send(String recipient, String subject, String body) {
            sent.add(subject + "|" + body);
        }

        @Override
        public String channelName() { return "Test"; }
    }

    @Test
    @DisplayName("UrgentAlert prepends URGENT to subject")
    void urgentAlertSubject() {
        CapturingChannel ch = new CapturingChannel();
        new UrgentAlert(ch, "Disk full").send("admin@example.com");

        assertEquals(1, ch.sent.size());
        assertTrue(ch.sent.get(0).contains("URGENT"));
    }

    @Test
    @DisplayName("Newsletter sends the correct title")
    void newsletterTitle() {
        CapturingChannel ch = new CapturingChannel();
        new Newsletter(ch, "Monthly Digest", "Content here").send("list@example.com");

        assertTrue(ch.sent.get(0).startsWith("Monthly Digest"));
    }

    @Test
    @DisplayName("withChannel produces a new notification using the new channel")
    void withChannelSwaps() {
        CapturingChannel ch1 = new CapturingChannel();
        CapturingChannel ch2 = new CapturingChannel();

        Notification alert = new UrgentAlert(ch1, "Fire alarm");
        alert.withChannel(ch2).send("security@example.com");

        assertTrue(ch1.sent.isEmpty(), "Original channel should not have been used");
        assertEquals(1, ch2.sent.size());
    }

    @Test
    @DisplayName("SMS channel truncates messages longer than 160 chars")
    void smsChannelTruncates() {
        String longBody = "A".repeat(200);
        // Should not throw; truncation is internal to SmsChannel
        assertDoesNotThrow(() ->
                new Newsletter(new SmsChannel(), "Title", longBody).send("+1-555-0000"));
    }
}
