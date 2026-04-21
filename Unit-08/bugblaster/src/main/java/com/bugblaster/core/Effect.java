package com.bugblaster.core;

import java.awt.Graphics2D;

/**
 * Abstract base class for short-lived visual effects (spray clouds, stomp rings,
 * charged blasts, drag trails).
 *
 * <p>Each effect has a finite tick lifetime and provides a linear alpha fade
 * through {@link #alpha()}. The {@link com.bugblaster.core.GameController}
 * advances all effects each tick and removes expired ones.
 */
public abstract class Effect {

    protected int tick;
    protected final int maxTicks;

    protected Effect(int maxTicks) {
        this.maxTicks = maxTicks;
    }

    /** Advances the effect animation by one tick. */
    public void update() { tick++; }

    /** Returns {@code true} when this effect should be removed from the scene. */
    public boolean isExpired() { return tick >= maxTicks; }

    /** Renders this effect. */
    public abstract void draw(Graphics2D g);

    /** Alpha value in [0, 1] that linearly decreases over the effect's lifetime. */
    protected float alpha() {
        return Math.max(0f, 1f - (float) tick / maxTicks);
    }

    /** Progress value in [0, 1] that linearly increases over the effect's lifetime. */
    protected float progress() {
        return Math.min(1f, (float) tick / maxTicks);
    }
}
