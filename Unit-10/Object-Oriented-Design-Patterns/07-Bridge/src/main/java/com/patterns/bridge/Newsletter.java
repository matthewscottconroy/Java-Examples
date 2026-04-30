package com.patterns.bridge;

/**
 * Refined Abstraction — a periodic newsletter notification.
 *
 * <p>A newsletter has a title and a longer body. When delivered via SMS it is
 * automatically truncated by the {@link SmsChannel} implementation.
 */
public class Newsletter extends Notification {

    private final String title;
    private final String body;

    /**
     * @param channel the delivery channel
     * @param title   the newsletter title
     * @param body    the newsletter body text
     */
    public Newsletter(MessageChannel channel, String title, String body) {
        super(channel);
        this.title = title;
        this.body  = body;
    }

    @Override
    public void send(String recipient) {
        channel.send(recipient, title, body);
    }

    @Override
    public Notification withChannel(MessageChannel newChannel) {
        return new Newsletter(newChannel, title, body);
    }
}
