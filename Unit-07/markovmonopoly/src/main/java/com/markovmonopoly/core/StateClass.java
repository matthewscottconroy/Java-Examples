package com.markovmonopoly.core;

/**
 * Classification of a state in a Markov chain.
 *
 * <p>Every state in a Markov chain can be categorized by its long-run behavior:
 * <ul>
 *   <li><b>ABSORBING</b> – Once entered, the chain never leaves. The self-loop
 *       probability equals exactly 1.0.</li>
 *   <li><b>RECURRENT</b> – Starting from this state, the chain is guaranteed to
 *       return to it with probability 1. All states in a closed communicating class
 *       (one with no outgoing edges to other classes) are recurrent.</li>
 *   <li><b>TRANSIENT</b> – There is positive probability of never returning to this
 *       state once it is left. The chain will eventually leave the communicating
 *       class containing this state and never come back.</li>
 * </ul>
 *
 * <p>Note: an ABSORBING state is technically a special case of a recurrent state
 * (it trivially returns to itself every step), but it is distinguished here because
 * it has its own special analysis methods (absorption probabilities, expected
 * time to absorption).
 */
public enum StateClass {

    /** Self-loop probability is 1.0; the chain never leaves once it arrives. */
    ABSORBING,

    /** The chain returns to this state with probability 1 (but may take many steps). */
    RECURRENT,

    /** There is positive probability of the chain never returning to this state. */
    TRANSIENT
}
