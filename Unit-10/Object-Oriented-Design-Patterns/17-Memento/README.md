# 17 — Memento: The Text Editor Undo System

## The Story

You are typing a document. You type "Hello, world!" — then accidentally delete
the last two words. You press **Ctrl+Z**. The editor rewinds to "Hello," as if
the deletion never happened. You press **Ctrl+Z** again and it becomes "Hello".
You change your mind and press **Ctrl+Y** — "Hello," is restored.

Behind every Ctrl+Z is a saved snapshot. Before every edit, the editor quietly
captures the entire document state into a small opaque **Memento** object.
When you undo, the editor restores the previous snapshot. The undo stack is just
a pile of these snapshots.

The key constraint: the `UndoManager` (caretaker) holds the snapshots but cannot
read them. Only the `TextDocument` (originator) can create or interpret a
`DocumentMemento`. This preserves encapsulation — the document's internal
representation is never exposed.

---

## The Problem It Solves

How do you capture an object's internal state for later restoration, without
violating encapsulation? If you expose the state publicly, you couple the caller
to implementation details. The Memento provides the snapshot without revealing
what is inside it.

---

## Structure

```
TextDocument     ← Originator (creates and restores mementos)
DocumentMemento  ← Memento (opaque snapshot, package-private)
UndoManager      ← Caretaker (holds the undo/redo stacks, never reads mementos)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Undo / redo | Text editors, drawing apps, game save states |
| Transaction rollback | Database operations that may need to be reversed |
| Snapshot and restore | Configuration snapshots, state machine checkpoints |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
