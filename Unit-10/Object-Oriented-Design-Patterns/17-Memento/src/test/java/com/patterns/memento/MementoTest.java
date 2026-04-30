package com.patterns.memento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Memento pattern — Text Editor Undo/Redo.
 */
class MementoTest {

    private TextDocument doc;
    private UndoManager  manager;

    @BeforeEach
    void setUp() {
        doc     = new TextDocument();
        manager = new UndoManager(doc);
    }

    private void edit(Runnable action) {
        manager.saveState();
        action.run();
    }

    @Test
    @DisplayName("type inserts text and advances cursor")
    void typeInsertsText() {
        doc.type("Hello");
        assertEquals("Hello", doc.getContent());
        assertEquals(5, doc.getCursorPosition());
    }

    @Test
    @DisplayName("undo restores previous state")
    void undoRestoresPrevious() {
        edit(() -> doc.type("Hello"));
        edit(() -> doc.type(" World"));
        manager.undo();
        assertEquals("Hello", doc.getContent());
    }

    @Test
    @DisplayName("redo re-applies undone edit")
    void redoReApplies() {
        edit(() -> doc.type("Hello"));
        edit(() -> doc.type(" World"));
        manager.undo();
        manager.redo();
        assertEquals("Hello World", doc.getContent());
    }

    @Test
    @DisplayName("new edit clears redo stack")
    void newEditClearsRedo() {
        edit(() -> doc.type("Hello"));
        edit(() -> doc.type(" World"));
        manager.undo();
        edit(() -> doc.type(" Java")); // new edit after undo
        assertEquals(0, manager.redoDepth());
    }

    @Test
    @DisplayName("undo on empty stack returns false")
    void undoOnEmptyReturnsFalse() {
        assertFalse(manager.undo());
    }

    @Test
    @DisplayName("multiple undos walk back through history")
    void multipleUndos() {
        edit(() -> doc.type("A"));
        edit(() -> doc.type("B"));
        edit(() -> doc.type("C"));
        manager.undo();
        manager.undo();
        assertEquals("A", doc.getContent());
    }
}
