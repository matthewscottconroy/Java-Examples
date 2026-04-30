package com.patterns.di;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test double — stores sent messages in a list so tests can inspect them
 * without touching a network.
 */
public class InMemoryMailSender implements MailSender {

    public record SentMessage(String to, String subject, String body) {}

    private final List<SentMessage> sent = new ArrayList<>();

    @Override
    public void send(String to, String subject, String body) {
        sent.add(new SentMessage(to, subject, body));
    }

    public List<SentMessage> getSentMessages() {
        return Collections.unmodifiableList(sent);
    }

    public void clear() { sent.clear(); }
}
