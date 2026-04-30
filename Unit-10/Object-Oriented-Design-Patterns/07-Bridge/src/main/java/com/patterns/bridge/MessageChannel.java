package com.patterns.bridge;

/**
 * Implementor — the delivery channel abstraction.
 *
 * <p>In the Bridge pattern this is the <em>Implementor</em> interface.
 * It defines the low-level operations for actually sending bytes to a
 * recipient. The high-level {@link Notification} abstraction delegates
 * all delivery work here.
 */
public interface MessageChannel {

    /**
     * Sends a message to a recipient via this channel.
     *
     * @param recipient the address or identifier (email address, phone number, etc.)
     * @param subject   a short subject line or title
     * @param body      the full message body
     */
    void send(String recipient, String subject, String body);

    /** @return the channel name (e.g., "Email", "SMS") */
    String channelName();
}
