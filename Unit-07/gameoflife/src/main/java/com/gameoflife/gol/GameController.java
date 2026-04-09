package com.gameoflife.gol;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages simulation state, history, and step transitions.
 *
 * <p>History is stored as a deque of past states (not including the current state).
 * Forward stepping adds to the past deque; backward stepping pops from it.
 * History is capped at {@link #MAX_HISTORY} frames — stepping backward past that
 * point is not possible (the back button is disabled).
 *
 * <p>The controller is not thread-safe; all calls must happen on the EDT.
 */
public final class GameController {

    public static final int MAX_HISTORY = 1000;

    private final Deque<GridState>           pastHistory  = new ArrayDeque<>();
    private final Deque<GridState.StepResult> lastResults = new ArrayDeque<>();  // parallel stats

    private GridState current;
    private RuleSet   ruleSet;
    private int       generation;
    private int       lastBorn;
    private int       lastDied;

    // -------------------------------------------------------------------------
    // Construction / reset
    // -------------------------------------------------------------------------

    public GameController(GridState initial, RuleSet ruleSet) {
        this.current    = initial;
        this.ruleSet    = ruleSet;
        this.generation = 0;
    }

    /** Replaces the current state and clears history. */
    public void reset(GridState newState) {
        pastHistory.clear();
        lastResults.clear();
        current    = newState;
        generation = 0;
        lastBorn   = 0;
        lastDied   = 0;
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    /** Advances one generation. Returns the new state. */
    public GridState stepForward() {
        if (pastHistory.size() >= MAX_HISTORY) {
            pastHistory.removeFirst();
            if (!lastResults.isEmpty()) lastResults.removeFirst();
        }
        pastHistory.addLast(current);
        GridState.StepResult result = current.nextGeneration(ruleSet);
        lastResults.addLast(result);
        current    = result.state();
        lastBorn   = result.born();
        lastDied   = result.died();
        generation++;
        return current;
    }

    /** Goes back one generation. Returns the restored state, or current if at the start. */
    public GridState stepBackward() {
        if (pastHistory.isEmpty()) return current;
        if (!lastResults.isEmpty()) {
            GridState.StepResult prev = lastResults.removeLast();
            lastBorn = 0; lastDied = 0;  // undo stats not meaningful
        }
        current = pastHistory.removeLast();
        generation--;
        lastBorn = 0;
        lastDied = 0;
        return current;
    }

    /** Rewinds all the way to the oldest available state. */
    public void rewind() {
        if (pastHistory.isEmpty()) return;
        generation -= pastHistory.size();
        current = pastHistory.peekFirst();
        pastHistory.clear();
        lastResults.clear();
        lastBorn = 0;
        lastDied = 0;
    }

    /** Advances {@code n} generations without storing intermediate states in history.
     *  Useful for fast-forwarding. Only the state before the skip is added to history. */
    public GridState fastForward(int n) {
        if (n <= 0) return current;
        // Save a checkpoint before skipping
        if (pastHistory.size() >= MAX_HISTORY) pastHistory.removeFirst();
        pastHistory.addLast(current);

        for (int i = 0; i < n; i++) {
            GridState.StepResult result = current.nextGeneration(ruleSet);
            current  = result.state();
            lastBorn = result.born();
            lastDied = result.died();
            generation++;
        }
        return current;
    }

    // -------------------------------------------------------------------------
    // In-place edits (do not modify history)
    // -------------------------------------------------------------------------

    /** Toggles a cell without creating a history entry. */
    public void editToggle(int row, int col) {
        current = current.withToggle(row, col);
    }

    /** Paints a cell alive without creating a history entry. */
    public void editAlive(int row, int col) {
        current = current.withAlive(row, col);
    }

    /** Paints a cell dead without creating a history entry. */
    public void editDead(int row, int col) {
        current = current.withDead(row, col);
    }

    /** Places a pattern at the given position without creating a history entry. */
    public void placePattern(Pattern p, int row, int col, boolean centered) {
        current = current.withPattern(p, row, col, centered);
    }

    /** Clears all cells without creating a history entry. */
    public void clearBoard() {
        current    = current.cleared();
        generation = 0;
        lastBorn   = 0;
        lastDied   = 0;
        pastHistory.clear();
        lastResults.clear();
    }

    /** Fills the board with random cells. Clears history. */
    public void randomFill(double density, long seed) {
        pastHistory.clear();
        lastResults.clear();
        current    = current.randomFilled(density, seed);
        generation = 0;
        lastBorn   = 0;
        lastDied   = 0;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public GridState getCurrent()    { return current; }
    public RuleSet   getRuleSet()    { return ruleSet; }
    public int       getGeneration() { return generation; }
    public int       getLastBorn()   { return lastBorn; }
    public int       getLastDied()   { return lastDied; }
    public boolean   canGoBack()     { return !pastHistory.isEmpty(); }
    public int       historySize()   { return pastHistory.size(); }

    public void setRuleSet(RuleSet r) { this.ruleSet = r; }
}
