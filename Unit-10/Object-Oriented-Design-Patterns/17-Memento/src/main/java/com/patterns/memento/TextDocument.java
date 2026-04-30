package com.patterns.memento;

/**
 * Originator — the text document whose state is saved and restored.
 *
 * <p>The document exposes {@link #save()} to produce a memento (a snapshot)
 * and {@link #restore(DocumentMemento)} to rewind to a previous state.
 * It never exposes its internal fields directly.
 */
public class TextDocument {

    private String content        = "";
    private int    cursorPosition = 0;

    /**
     * Types the given text at the current cursor position and advances the cursor.
     *
     * @param text text to insert
     */
    public void type(String text) {
        content        = content.substring(0, cursorPosition) + text
                         + content.substring(cursorPosition);
        cursorPosition += text.length();
    }

    /**
     * Deletes the last {@code n} characters before the cursor.
     *
     * @param n number of characters to delete
     */
    public void delete(int n) {
        int from  = Math.max(0, cursorPosition - n);
        content        = content.substring(0, from) + content.substring(cursorPosition);
        cursorPosition = from;
    }

    /**
     * Creates a memento — a snapshot of the current document state.
     * Call this before every edit so the edit can be undone.
     *
     * @return an opaque snapshot
     */
    public DocumentMemento save() {
        return new DocumentMemento(content, cursorPosition);
    }

    /**
     * Restores the document to the state captured in the given memento.
     *
     * @param memento the snapshot to restore
     */
    public void restore(DocumentMemento memento) {
        this.content        = memento.getContent();
        this.cursorPosition = memento.getCursorPosition();
    }

    /** @return the full document content */
    public String getContent()        { return content; }

    /** @return the current cursor position */
    public int    getCursorPosition() { return cursorPosition; }

    @Override
    public String toString() {
        return "\"" + content + "\" [cursor=" + cursorPosition + "]";
    }
}
