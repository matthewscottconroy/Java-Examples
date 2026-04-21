package com.wizardrogue.core;

import java.awt.event.KeyEvent;

/**
 * The four cardinal movement directions, each carrying a unit delta and the
 * keyboard virtual-key code that triggers it.
 *
 * <p>Binding the VK code here keeps the mapping in one place; the panel
 * and controller never hard-code key constants for movement.
 */
public enum Direction {
    NORTH( 0, -1, KeyEvent.VK_W),
    SOUTH( 0,  1, KeyEvent.VK_S),
    WEST (-1,  0, KeyEvent.VK_A),
    EAST ( 1,  0, KeyEvent.VK_D);

    public final int dx;
    public final int dy;
    public final int vk;

    Direction(int dx, int dy, int vk) {
        this.dx = dx;
        this.dy = dy;
        this.vk = vk;
    }

    /** Returns the Direction whose WASD VK matches {@code keyCode}, or {@code null}. */
    public static Direction fromKey(int keyCode) {
        for (Direction d : values()) {
            if (d.vk == keyCode) return d;
        }
        return null;
    }

    /** Maps arrow keys to directions for facing-only rotation (no movement). */
    public static Direction fromArrowKey(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_UP    -> NORTH;
            case KeyEvent.VK_DOWN  -> SOUTH;
            case KeyEvent.VK_LEFT  -> WEST;
            case KeyEvent.VK_RIGHT -> EAST;
            default                -> null;
        };
    }

    /** Rotates 90° clockwise (N → E → S → W → N). */
    public Direction turnRight() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST  -> SOUTH;
            case SOUTH -> WEST;
            case WEST  -> NORTH;
        };
    }

    /** Rotates 90° counter-clockwise. */
    public Direction turnLeft() {
        return switch (this) {
            case NORTH -> WEST;
            case WEST  -> SOUTH;
            case SOUTH -> EAST;
            case EAST  -> NORTH;
        };
    }

    /** Returns the opposite direction. */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST  -> WEST;
            case WEST  -> EAST;
        };
    }
}
