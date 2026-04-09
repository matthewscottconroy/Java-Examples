package com.combustionengine.model;

/** The four strokes of a 4-stroke Otto-cycle engine. */
public enum CyclePhase {
    /** Piston descends; intake valve open; fresh charge drawn in. */
    INTAKE,
    /** Both valves closed; piston ascends; charge compressed adiabatically. */
    COMPRESSION,
    /** Spark fires at TDC; expanding combustion gases drive piston down. */
    POWER,
    /** Exhaust valve open; piston ascends; burned gases expelled. */
    EXHAUST
}
