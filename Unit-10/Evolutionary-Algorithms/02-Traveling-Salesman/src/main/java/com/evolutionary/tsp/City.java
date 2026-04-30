package com.evolutionary.tsp;

/**
 * A delivery stop with a name and 2D coordinates.
 */
public record City(String name, double x, double y) {

    public double distanceTo(City other) {
        double dx = this.x - other.x, dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
