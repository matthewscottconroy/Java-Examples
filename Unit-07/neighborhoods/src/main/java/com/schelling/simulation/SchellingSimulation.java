package com.schelling.simulation;

import com.schelling.model.AgentType;
import com.schelling.model.Grid;
import com.schelling.model.InitialCondition;
import com.schelling.model.NeighborhoodType;
import com.schelling.model.SimulationConfig;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Implements the Schelling segregation simulation.
 *
 * <h2>Algorithm (per step)</h2>
 * <ol>
 *   <li>Collect all unsatisfied occupied cells and all empty cells.</li>
 *   <li>Shuffle both lists independently.</li>
 *   <li>Move each unsatisfied agent to a distinct empty cell
 *       (up to {@code min(|unsatisfied|, |empty|)} moves).</li>
 * </ol>
 *
 * <h2>Live threshold update</h2>
 * <p>Call {@link #setLiveThresholds(double, double)} to change the satisfaction
 * thresholds without a full reset.  This affects all subsequent calls to
 * {@link #step()}, {@link #isStable()}, and {@link #getUnsatisfiedCells()}.
 */
public final class SchellingSimulation {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final SimulationConfig config;
    private Grid             grid;
    private int              stepCount;
    private Random           random;

    // Live (overridable) thresholds and neighborhood — can change without reset
    private double           liveThresholdA;
    private double           liveThresholdB;
    private NeighborhoodType neighborhoodType;

    private int lastMoveCount = 0;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public SchellingSimulation(SimulationConfig config) {
        if (config == null) throw new NullPointerException("config must not be null");
        this.config = config;
        reset();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Advances the simulation by one step.
     *
     * @return the number of agents that moved
     */
    public int step() {
        List<Point> unsatisfied = new ArrayList<>(getUnsatisfiedCells());
        if (unsatisfied.isEmpty()) { lastMoveCount = 0; return 0; }

        List<Point> empty = new ArrayList<>(grid.getEmptyCells());
        Collections.shuffle(unsatisfied, random);
        Collections.shuffle(empty, random);

        int moves = Math.min(unsatisfied.size(), empty.size());
        for (int i = 0; i < moves; i++) {
            Point from = unsatisfied.get(i);
            Point to   = empty.get(i);
            grid.setCell(to.x,   to.y,   grid.getCell(from.x, from.y));
            grid.setCell(from.x, from.y, null);
        }

        stepCount++;
        lastMoveCount = moves;
        return moves;
    }

    /** Resets the simulation from the original configuration. */
    public void reset() {
        this.random          = new Random(config.getRandomSeed());
        this.grid            = buildInitialGrid();
        this.stepCount       = 0;
        this.lastMoveCount   = 0;
        this.liveThresholdA  = config.getThresholdA();
        this.liveThresholdB  = config.getThresholdB();
        this.neighborhoodType = config.getNeighborhoodType();
    }

    /**
     * Updates the satisfaction thresholds without resetting the grid.
     * Takes effect immediately for all subsequent operations.
     */
    public void setLiveThresholds(double tA, double tB) {
        this.liveThresholdA = Math.max(0.0, Math.min(1.0, tA));
        this.liveThresholdB = Math.max(0.0, Math.min(1.0, tB));
    }

    /** Updates the neighborhood type without resetting the grid. */
    public void setLiveNeighborhoodType(NeighborhoodType nt) {
        this.neighborhoodType = nt;
    }

    /** Returns {@code true} if no unsatisfied agents remain. */
    public boolean isStable() {
        return getUnsatisfiedCells().isEmpty();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Grid   getGrid()          { return grid; }
    public SimulationConfig getConfig() { return config; }
    public int    getStepCount()     { return stepCount; }
    public int    getLastMoveCount() { return lastMoveCount; }
    public double getLiveThresholdA(){ return liveThresholdA; }
    public double getLiveThresholdB(){ return liveThresholdB; }

    /**
     * Returns the current fraction of occupied cells that are satisfied,
     * using the live per-type thresholds.
     */
    public double getSatisfactionRate() {
        List<Point> occupied = grid.getOccupiedCells();
        if (occupied.isEmpty()) return 1.0;
        long satisfied = occupied.stream().filter(p -> {
            AgentType t  = grid.getCell(p.x, p.y);
            double    thr = (t == AgentType.TYPE_A) ? liveThresholdA : liveThresholdB;
            return grid.isSatisfied(p.x, p.y, thr, neighborhoodType);
        }).count();
        return (double) satisfied / occupied.size();
    }

    /**
     * Returns all unsatisfied agent coordinates using the live thresholds and
     * neighborhood type.
     */
    public List<Point> getUnsatisfiedCells() {
        List<Point> result = new ArrayList<>();
        for (Point p : grid.getOccupiedCells()) {
            AgentType t   = grid.getCell(p.x, p.y);
            double    thr = (t == AgentType.TYPE_A) ? liveThresholdA : liveThresholdB;
            if (!grid.isSatisfied(p.x, p.y, thr, neighborhoodType))
                result.add(p);
        }
        return Collections.unmodifiableList(result);
    }

    // -------------------------------------------------------------------------
    // Segregation metrics
    // -------------------------------------------------------------------------

    /**
     * Returns the Duncan dissimilarity index for the current grid state.
     * @see Grid#getDissimilarityIndex()
     */
    public double getDissimilarityIndex() {
        return grid.getDissimilarityIndex();
    }

    /**
     * Returns the isolation index for the given agent type.
     * @see Grid#getIsolationIndex(AgentType, NeighborhoodType)
     */
    public double getIsolationIndex(AgentType type) {
        return grid.getIsolationIndex(type, neighborhoodType);
    }

    // -------------------------------------------------------------------------
    // Initial grid builders
    // -------------------------------------------------------------------------

    private Grid buildInitialGrid() {
        return switch (config.getInitialCondition()) {
            case RANDOM      -> buildRandomGrid();
            case SEGREGATED  -> buildSegregatedGrid();
            case CHECKERBOARD-> buildCheckerboardGrid();
            case ENCLAVE     -> buildEnclaveGrid();
            case CLUSTERS    -> buildClustersGrid();
        };
    }

    private Grid buildRandomGrid() {
        int totalCells    = config.getRows() * config.getCols();
        int emptyCells    = (int) Math.round(totalCells * config.getEmptyFraction());
        int occupiedCells = totalCells - emptyCells;
        int typeBCount    = (int) Math.round(occupiedCells * config.getTypeBFraction());
        int typeACount    = occupiedCells - typeBCount;

        List<AgentType> allTypes = new ArrayList<>(totalCells);
        for (int i = 0; i < typeACount; i++) allTypes.add(AgentType.TYPE_A);
        for (int i = 0; i < typeBCount; i++) allTypes.add(AgentType.TYPE_B);
        for (int i = 0; i < emptyCells; i++) allTypes.add(null);
        Collections.shuffle(allTypes, random);

        Grid g = new Grid(config.getRows(), config.getCols());
        int idx = 0;
        for (int r = 0; r < config.getRows(); r++)
            for (int c = 0; c < config.getCols(); c++)
                g.setCell(r, c, allTypes.get(idx++));
        return g;
    }

    private Grid buildSegregatedGrid() {
        // Group A on the left half, Group B on the right half; empty fraction applied randomly
        Grid g       = new Grid(config.getRows(), config.getCols());
        int  halfCol = config.getCols() / 2;
        for (int r = 0; r < config.getRows(); r++)
            for (int c = 0; c < config.getCols(); c++) {
                if (random.nextDouble() < config.getEmptyFraction())
                    g.setCell(r, c, null);
                else
                    g.setCell(r, c, c < halfCol ? AgentType.TYPE_A : AgentType.TYPE_B);
            }
        return g;
    }

    private Grid buildCheckerboardGrid() {
        Grid g = new Grid(config.getRows(), config.getCols());
        for (int r = 0; r < config.getRows(); r++)
            for (int c = 0; c < config.getCols(); c++) {
                if (random.nextDouble() < config.getEmptyFraction())
                    g.setCell(r, c, null);
                else
                    g.setCell(r, c, (r + c) % 2 == 0 ? AgentType.TYPE_A : AgentType.TYPE_B);
            }
        return g;
    }

    private Grid buildEnclaveGrid() {
        // Group B in a central disc; Group A everywhere else
        Grid g        = new Grid(config.getRows(), config.getCols());
        int  centerR  = config.getRows() / 2;
        int  centerC  = config.getCols() / 2;
        int  radius   = Math.min(config.getRows(), config.getCols()) / 5;
        int  radius2  = radius * radius;
        for (int r = 0; r < config.getRows(); r++)
            for (int c = 0; c < config.getCols(); c++) {
                if (random.nextDouble() < config.getEmptyFraction())
                    g.setCell(r, c, null);
                else {
                    int dr = r - centerR, dc = c - centerC;
                    g.setCell(r, c, (dr*dr + dc*dc <= radius2) ? AgentType.TYPE_B : AgentType.TYPE_A);
                }
            }
        return g;
    }

    private Grid buildClustersGrid() {
        // Voronoi-seeded clusters: random seed points assigned alternately to A/B
        int numSeeds = Math.max(6, Math.min(24, config.getRows() / 4));
        int[] seedR  = new int[numSeeds];
        int[] seedC  = new int[numSeeds];
        AgentType[] seedType = new AgentType[numSeeds];
        for (int i = 0; i < numSeeds; i++) {
            seedR[i]    = random.nextInt(config.getRows());
            seedC[i]    = random.nextInt(config.getCols());
            seedType[i] = (i % 2 == 0) ? AgentType.TYPE_A : AgentType.TYPE_B;
        }
        // Shuffle assignments so it's not simply alternating spatially
        List<AgentType> types = new ArrayList<>(List.of(seedType));
        Collections.shuffle(types, random);
        for (int i = 0; i < numSeeds; i++) seedType[i] = types.get(i);

        Grid g = new Grid(config.getRows(), config.getCols());
        for (int r = 0; r < config.getRows(); r++)
            for (int c = 0; c < config.getCols(); c++) {
                if (random.nextDouble() < config.getEmptyFraction()) {
                    g.setCell(r, c, null);
                } else {
                    int nearest = 0, minDist = Integer.MAX_VALUE;
                    for (int i = 0; i < numSeeds; i++) {
                        int d = (r - seedR[i]) * (r - seedR[i]) + (c - seedC[i]) * (c - seedC[i]);
                        if (d < minDist) { minDist = d; nearest = i; }
                    }
                    g.setCell(r, c, seedType[nearest]);
                }
            }
        return g;
    }
}
