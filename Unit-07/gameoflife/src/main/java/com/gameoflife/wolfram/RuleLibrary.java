package com.gameoflife.wolfram;

import java.util.List;

/**
 * A curated library of notable Wolfram elementary cellular automaton rules.
 */
public final class RuleLibrary {

    public record RuleEntry(
        WolframRule rule,
        CAHistory.InitCondition defaultInit,
        String shortName,
        String description
    ) {}

    public static final List<RuleEntry> ENTRIES = List.of(

        new RuleEntry(new WolframRule(30), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 30 — Chaos",
            "Class 3 (chaotic). Starting from a single cell, produces a pattern " +
            "that appears random and passes statistical tests for randomness. " +
            "Used in Mathematica's built-in pseudo-random number generator. " +
            "The left column perfectly encodes the digits of pi. " +
            "Wolfram's central example for complexity from simple rules."),

        new RuleEntry(new WolframRule(90), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 90 — Sierpiński",
            "Class 3. An additive rule: the new cell is the XOR of its two outer neighbors. " +
            "Produces the Sierpiński triangle fractal from a single seed cell. " +
            "The pattern at generation 2^n - 1 replicates a scaled copy of the original. " +
            "Related to Pascal's triangle modulo 2."),

        new RuleEntry(new WolframRule(110), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 110 — Turing Complete",
            "Class 4 (complex, localized structures). Matthew Cook proved in 1994 " +
            "(published 2004) that Rule 110 is Turing complete — it can simulate any " +
            "computation given suitable initial conditions. Uses 'gliders' (moving " +
            "structures) that interact to perform logic. The simplest known Turing-complete system."),

        new RuleEntry(new WolframRule(184), CAHistory.InitCondition.RANDOM_MEDIUM,
            "Rule 184 — Traffic Flow",
            "Class 2. Models single-lane traffic: 1s are cars moving right, 0s are empty road. " +
            "Cars advance if the next cell is empty; otherwise they wait. " +
            "Produces emergent traffic jams from random initial conditions. " +
            "Used in theoretical traffic models and particle-antiparticle annihilation."),

        new RuleEntry(new WolframRule(150), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 150 — Pascal's Triangle",
            "Class 3. Another additive rule (XOR of all three neighbors). " +
            "Produces a symmetric pattern closely related to Pascal's triangle mod 2 " +
            "and the Sierpiński triangle, but with different internal structure than Rule 90."),

        new RuleEntry(new WolframRule(126), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 126 — Complex Waves",
            "Class 3. Produces complex wave-like interference patterns from a single seed. " +
            "Has a rich variety of triangular regions and propagating structures."),

        new RuleEntry(new WolframRule(54), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 54 — Localized Structures",
            "Class 4. Produces persistent localized structures (glider-like particles) " +
            "embedded in a periodic background. One of the most complex among elementary CAs."),

        new RuleEntry(new WolframRule(45), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 45 — Asymmetric Chaos",
            "Class 3. Produces chaotic behavior like Rule 30 but with broken left-right " +
            "symmetry. The rule table is asymmetric between 0-neighbors and 1-neighbors."),

        new RuleEntry(new WolframRule(22), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 22 — Nested Structure",
            "Class 3. Produces a self-similar nested pattern with pronounced triangular " +
            "regions. Symmetric (the rule is self-complementary about cell state)."),

        new RuleEntry(new WolframRule(18), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 18 — Simple Fractal",
            "Class 3. Produces a clean fractal-like pattern with clear triangular holes. " +
            "Simpler and more regular than Rule 30 but still technically non-periodic."),

        new RuleEntry(new WolframRule(51), CAHistory.InitCondition.ALTERNATING,
            "Rule 51 — Complement",
            "Class 2. Inverts every cell each generation. The simplest non-trivial period-2 rule. " +
            "Every pattern has period 2 (all patterns return to their complement then back)."),

        new RuleEntry(new WolframRule(0), CAHistory.InitCondition.RANDOM_MEDIUM,
            "Rule 0 — All Die",
            "Class 1. Every cell immediately becomes 0. The simplest rule — " +
            "demonstrates that a single rule application sends everything to death."),

        new RuleEntry(new WolframRule(255), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 255 — All Live",
            "Class 1. Every cell immediately becomes 1. The complement of Rule 0."),

        new RuleEntry(new WolframRule(86), CAHistory.InitCondition.SINGLE_CENTER,
            "Rule 86 — Right Triangle",
            "Class 3. Produces a clean right-triangle pattern expanding to the right, " +
            "demonstrating strong directional asymmetry in an elementary rule."),

        new RuleEntry(new WolframRule(73), CAHistory.InitCondition.RANDOM_SPARSE,
            "Rule 73 — Periodic Islands",
            "Class 2. From random initial conditions, settles into periodic repeating " +
            "structures separated by fixed dead zones.")
    );

    /** Finds the library entry for a given rule number, or null. */
    public static RuleEntry find(int ruleNumber) {
        return ENTRIES.stream()
            .filter(e -> e.rule().number() == ruleNumber)
            .findFirst()
            .orElse(null);
    }

    private RuleLibrary() {}
}
