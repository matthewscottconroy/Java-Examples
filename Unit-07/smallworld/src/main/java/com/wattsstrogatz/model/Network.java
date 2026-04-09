package com.wattsstrogatz.model;

import java.util.*;

/**
 * Undirected network backed by both an edge list and adjacency sets.
 *
 * <p>Use {@link #ringLattice(int, int)} to create the initial Watts-Strogatz
 * substrate: n nodes in a ring, each connected to k nearest neighbours on
 * each side (total degree = 2k).
 */
public final class Network {

    private final int            nodeCount;
    private final List<Edge>     edges;
    private final Set<Integer>[] adjacency;

    /**
     * Creates an empty network with nodeCount isolated nodes.
     *
     * @param nodeCount number of nodes, must be >= 2
     */
    @SuppressWarnings("unchecked")
    public Network(int nodeCount) {
        if (nodeCount < 2)
            throw new IllegalArgumentException("nodeCount must be >= 2");
        this.nodeCount = nodeCount;
        this.edges     = new ArrayList<>();
        this.adjacency = new HashSet[nodeCount];
        for (int i = 0; i < nodeCount; i++)
            adjacency[i] = new HashSet<>();
    }

    /**
     * Builds a ring lattice: n nodes in a cycle, each connected to its k
     * nearest neighbours on each side (total degree = 2k).
     *
     * @param n number of nodes
     * @param k half-degree
     * @return ring lattice network
     * @throws IllegalArgumentException if n <= 2k or k < 1
     */
    public static Network ringLattice(int n, int k) {
        if (k < 1)  throw new IllegalArgumentException("k must be >= 1");
        if (n <= 2 * k) throw new IllegalArgumentException("n must be > 2k");
        Network net = new Network(n);
        for (int i = 0; i < n; i++)
            for (int j = 1; j <= k; j++)
                net.addEdge(new Edge(i, (i + j) % n));
        return net;
    }

    /**
     * Adds an edge and updates both adjacency sets.
     *
     * @param edge the edge to add
     */
    public void addEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge must not be null");
        edges.add(edge);
        adjacency[edge.getU()].add(edge.getV());
        adjacency[edge.getV()].add(edge.getU());
    }

    /**
     * Rewires an existing edge to new endpoints, updating adjacency sets.
     *
     * @param edge the edge to rewire
     * @param newU new source endpoint
     * @param newV new target endpoint
     */
    public void rewireEdge(Edge edge, int newU, int newV) {
        adjacency[edge.getU()].remove(edge.getV());
        adjacency[edge.getV()].remove(edge.getU());
        edge.rewire(newU, newV);
        adjacency[newU].add(newV);
        adjacency[newV].add(newU);
    }

    /**
     * Returns true if an edge exists between u and v.
     *
     * @param u one endpoint
     * @param v the other endpoint
     * @return true if connected
     */
    public boolean hasEdge(int u, int v) {
        checkNode(u); checkNode(v);
        return adjacency[u].contains(v);
    }

    /**
     * Returns an unmodifiable view of the neighbours of node u.
     *
     * @param u node index
     * @return set of neighbour indices
     */
    public Set<Integer> neighbours(int u) {
        checkNode(u);
        return Collections.unmodifiableSet(adjacency[u]);
    }

    /**
     * Returns the degree of node u.
     *
     * @param u node index
     * @return degree
     */
    public int degree(int u) {
        checkNode(u);
        return adjacency[u].size();
    }

    /** @return number of nodes */
    public int getNodeCount() { return nodeCount; }

    /** @return number of edges */
    public int getEdgeCount() { return edges.size(); }

    /** @return unmodifiable view of all edges */
    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }

    /**
     * Returns the number of edges that have been rewired.
     *
     * @return rewired edge count
     */
    public long getRewiredEdgeCount() {
        return edges.stream().filter(Edge::isRewired).count();
    }

    private void checkNode(int u) {
        if (u < 0 || u >= nodeCount)
            throw new IndexOutOfBoundsException(
                "Node " + u + " out of range [0, " + nodeCount + ")");
    }

    @Override
    public String toString() {
        return String.format("Network{n=%d, edges=%d, rewired=%d}",
            nodeCount, edges.size(), getRewiredEdgeCount());
    }
}
