package com.evolutionary.foundations;

import java.util.Arrays;
import java.util.Random;

/**
 * A binary-string chromosome for the genetic algorithm.
 *
 * Each bit represents one boolean choice (e.g., "include this feature?").
 * The chromosome is immutable — crossover and mutation produce new individuals.
 */
public record Individual(boolean[] genes, double fitness) {

    /** Creates a random individual with the given chromosome length. */
    public static Individual random(int length, Random rng) {
        boolean[] genes = new boolean[length];
        for (int i = 0; i < length; i++) genes[i] = rng.nextBoolean();
        return new Individual(genes, 0.0);
    }

    /** Returns a copy of this individual with the fitness assigned. */
    public Individual withFitness(double fitness) {
        return new Individual(genes.clone(), fitness);
    }

    public int length() { return genes.length; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (boolean g : genes) sb.append(g ? '1' : '0');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Individual other)) return false;
        return Arrays.equals(genes, other.genes);
    }

    @Override
    public int hashCode() { return Arrays.hashCode(genes); }
}
