package com.wizardrogue.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * A scrolling log of recent game messages displayed in the panel below the dungeon.
 *
 * <p>Messages are prepended so the most recent is always at index 0. The log
 * is capped at {@value #CAPACITY} entries to prevent unbounded growth. The UI
 * displays only the last {@value #DISPLAY_LINES} entries.
 */
public final class MessageLog {

    static final int CAPACITY      = 200;
    static final int DISPLAY_LINES = 5;

    private final Deque<String> messages = new ArrayDeque<>();

    /** Adds a message to the front of the log. */
    public void add(String message) {
        messages.addFirst(message);
        if (messages.size() > CAPACITY) messages.removeLast();
    }

    /**
     * Returns up to {@value #DISPLAY_LINES} recent messages, newest first.
     * The returned list is a snapshot — safe to iterate while the log is modified.
     */
    public List<String> getRecent() {
        List<String> result = new ArrayList<>();
        int count = 0;
        for (String m : messages) {
            if (count++ >= DISPLAY_LINES) break;
            result.add(m);
        }
        return result;
    }

    public void clear() { messages.clear(); }
}
