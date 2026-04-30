package com.patterns.memento;

/**
 * Memento — an opaque snapshot of a document's state at a point in time.
 *
 * <p>The memento is immutable and package-private: only the {@link TextDocument}
 * originator can create or read it. The {@link UndoManager} caretaker stores
 * mementos but cannot inspect their contents — it only holds them.
 *
 * <p>This encapsulation is the whole point: the internal state of
 * {@code TextDocument} is captured without leaking its representation to
 * anything outside the originator.
 */
final class DocumentMemento {

    private final String content;
    private final int    cursorPosition;

    DocumentMemento(String content, int cursorPosition) {
        this.content        = content;
        this.cursorPosition = cursorPosition;
    }

    String getContent()        { return content; }
    int    getCursorPosition() { return cursorPosition; }
}
