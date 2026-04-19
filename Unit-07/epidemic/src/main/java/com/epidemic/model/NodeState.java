package com.epidemic.model;

/**
 * Discrete health state for a single node in the SIR epidemic model.
 *
 * <p>Nodes transition only in the direction {@code SUSCEPTIBLE → INFECTED → RECOVERED}.
 * Once a node reaches {@code RECOVERED} it is permanently immune and cannot
 * become susceptible again.
 */
public enum NodeState {

    /** Node has not yet been exposed; can be infected by infectious neighbours. */
    SUSCEPTIBLE,

    /** Node is actively infectious and can spread the pathogen to susceptible neighbours. */
    INFECTED,

    /** Node has cleared the infection and is permanently immune. */
    RECOVERED
}
