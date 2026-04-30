package com.exam;

/**
 * Question difficulty tier.
 *
 * <p>Each tier carries a default point value used on the exam printout.
 * The generator targets one easy and one hard question per section (when
 * per-type >= 2) so each exam has a calibrated spread.
 */
public enum Difficulty {
    EASY(5),
    MEDIUM(10),
    HARD(15);

    public final int points;

    Difficulty(int points) { this.points = points; }
}
