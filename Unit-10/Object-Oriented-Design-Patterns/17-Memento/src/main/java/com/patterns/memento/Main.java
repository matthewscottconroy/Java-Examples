package com.patterns.memento;

/**
 * Demonstrates the Memento pattern with a text editor undo system.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Text Editor Undo / Redo (Memento Pattern) ===\n");

        TextDocument doc    = new TextDocument();
        UndoManager  manager = new UndoManager(doc);

        // Type a sentence, saving state before each edit
        edit(manager, doc, () -> doc.type("Hello"));
        System.out.println("After type 'Hello':     " + doc);

        edit(manager, doc, () -> doc.type(", world"));
        System.out.println("After type ', world':   " + doc);

        edit(manager, doc, () -> doc.type("!"));
        System.out.println("After type '!':         " + doc);

        // Oops — undo the '!'
        System.out.println("\n--- Ctrl+Z (undo) ---");
        manager.undo();
        System.out.println("After undo:             " + doc);

        // Undo ', world' too
        manager.undo();
        System.out.println("After 2nd undo:         " + doc);

        // Redo once
        System.out.println("\n--- Ctrl+Y (redo) ---");
        manager.redo();
        System.out.println("After redo:             " + doc);

        System.out.println("\nUndo depth: " + manager.undoDepth()
                + " | Redo depth: " + manager.redoDepth());
    }

    private static void edit(UndoManager m, TextDocument doc, Runnable edit) {
        m.saveState();
        edit.run();
    }
}
