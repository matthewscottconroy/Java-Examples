package com.patterns.bridge;

/**
 * Refined Abstraction — an urgent alert notification.
 *
 * <p>The body is always prepended with "URGENT:" to ensure it stands out,
 * regardless of which channel delivers it.
 */
public class UrgentAlert extends Notification {

    private final String message;

    /**
     * @param channel the delivery channel
     * @param message the alert message
     */
    public UrgentAlert(MessageChannel channel, String message) {
        super(channel);
        this.message = message;
    }

    @Override
    public void send(String recipient) {
        channel.send(recipient, "URGENT ALERT", "*** " + message + " ***");
    }

    @Override
    public Notification withChannel(MessageChannel newChannel) {
        return new UrgentAlert(newChannel, message);
    }
}
