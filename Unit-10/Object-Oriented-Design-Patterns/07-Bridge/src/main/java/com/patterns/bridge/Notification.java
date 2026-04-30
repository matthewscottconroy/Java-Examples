package com.patterns.bridge;

/**
 * Abstraction — a notification that can be sent over any {@link MessageChannel}.
 *
 * <p>The Bridge separates two independent dimensions of variation:
 * <ul>
 *   <li><b>What</b> kind of notification (urgent alert, newsletter, …)</li>
 *   <li><b>How</b> it is delivered (email, SMS, push, …)</li>
 * </ul>
 *
 * <p>Adding a new notification type (e.g., {@code PromotionalNotification})
 * requires no changes to any channel. Adding a new channel (e.g., Slack)
 * requires no changes to any notification type. The two hierarchies vary
 * completely independently — that is the Bridge.
 */
public abstract class Notification {

    /** The delivery channel — the implementation side of the bridge. */
    protected final MessageChannel channel;

    /**
     * @param channel the channel to deliver this notification through
     */
    protected Notification(MessageChannel channel) {
        this.channel = channel;
    }

    /**
     * Sends this notification to the specified recipient.
     *
     * @param recipient the address or identifier of the recipient
     */
    public abstract void send(String recipient);

    /**
     * Changes the delivery channel at runtime — the abstraction stays the same.
     *
     * @param newChannel the replacement channel
     * @return a new Notification of the same type over the new channel
     */
    public abstract Notification withChannel(MessageChannel newChannel);
}
