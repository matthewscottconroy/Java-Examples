package com.patterns.memento;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Caretaker — manages the undo and redo stacks.
 *
 * <p>The caretaker stores {@link DocumentMemento} objects but never reads their
 * contents. It is responsible for the lifecycle of mementos (save before edit,
 * restore on undo, re-save on redo).
 */
public class UndoManager {

    private final TextDocument         document;
    private final Deque<DocumentMemento> undoStack = new ArrayDeque<>();
    private final Deque<DocumentMemento> redoStack = new ArrayDeque<>();

    /**
     * @param document the document to manage undo/redo for
     */
    public UndoManager(TextDocument document) {
        this.document = document;
    }

    /**
     * Saves the current document state before an edit.
     * Clears the redo stack (a new edit invalidates the redo history).
     */
    public void saveState() {
        undoStack.push(document.save());
        redoStack.clear();
    }

    /**
     * Undoes the last edit: pops the undo stack and restores the saved state.
     * The current state is pushed onto the redo stack.
     *
     * @return true if undo succeeded, false if nothing to undo
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            System.out.println("  [Undo] Nothing to undo.");
            return false;
        }
        redoStack.push(document.save());
        document.restore(undoStack.pop());
        return true;
    }

    /**
     * Redoes the last undone edit.
     *
     * @return true if redo succeeded, false if nothing to redo
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            System.out.println("  [Redo] Nothing to redo.");
            return false;
        }
        undoStack.push(document.save());
        document.restore(redoStack.pop());
        return true;
    }

    /** @return the number of available undo steps */
    public int undoDepth() { return undoStack.size(); }

    /** @return the number of available redo steps */
    public int redoDepth() { return redoStack.size(); }
}
