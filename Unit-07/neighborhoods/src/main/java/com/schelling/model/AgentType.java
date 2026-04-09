package com.schelling.model;

import java.awt.Color;

/**
 * Represents the two agent populations in the Schelling segregation model.
 * Each type carries a display color used for grid rendering.
 */
public enum AgentType {

    /** First population — rendered in steel blue. */
    TYPE_A(new Color(70, 130, 180), "Group A"),

    /** Second population — rendered in Indian red. */
    TYPE_B(new Color(205, 92, 92), "Group B");

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final Color displayColor;
    private final String displayName;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    AgentType(Color displayColor, String displayName) {
        this.displayColor = displayColor;
        this.displayName  = displayName;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the AWT color used when rendering a cell of this type.
     *
     * @return display color, never {@code null}
     */
    public Color getDisplayColor() {
        return displayColor;
    }

    /**
     * Returns the human-readable label for this agent type.
     *
     * @return display name, never {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the other agent type (i.e., the "opposite" group).
     *
     * @return {@code TYPE_B} if called on {@code TYPE_A}, and vice versa
     */
    public AgentType opposite() {
        return this == TYPE_A ? TYPE_B : TYPE_A;
    }
}
