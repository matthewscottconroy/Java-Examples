package com.wattsstrogatz.model;

/**
 * An undirected edge in the Watts-Strogatz network.
 * Tracks current endpoints plus whether the edge has been rewired from
 * its original ring-lattice position.
 */
public final class Edge {

    private int u;
    private int v;
    private final int originalU;
    private boolean rewired;

    /**
     * Creates a lattice edge between u and v.
     *
     * @param u one endpoint
     * @param v the other endpoint
     */
    public Edge(int u, int v) {
        this.u         = u;
        this.v         = v;
        this.originalU = u;
        this.rewired   = false;
    }

    /**
     * Rewires this edge to new endpoints.
     *
     * @param newU new source endpoint
     * @param newV new target endpoint
     */
    public void rewire(int newU, int newV) {
        this.u       = newU;
        this.v       = newV;
        this.rewired = true;
    }

    /** @return current source endpoint */
    public int getU() { return u; }

    /** @return current target endpoint */
    public int getV() { return v; }

    /** @return original source vertex from the ring lattice */
    public int getOriginalU() { return originalU; }

    /** @return true if this edge has been rewired away from its lattice target */
    public boolean isRewired() { return rewired; }

    /**
     * Returns true if this edge connects a and b in either direction.
     *
     * @param a one endpoint
     * @param b the other endpoint
     * @return true if both endpoints match
     */
    public boolean connects(int a, int b) {
        return (u == a && v == b) || (u == b && v == a);
    }

    @Override
    public String toString() {
        return String.format("Edge(%d-%d%s)", u, v, rewired ? "*" : "");
    }
}
