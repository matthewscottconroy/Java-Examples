package com.algorithms.sorting.mergesort;

import java.time.LocalDateTime;

/**
 * @param id        medical record number
 * @param surname   patient surname
 * @param admitTime when the patient was admitted
 * @param acuity    triage score 1 (critical) – 5 (minor)
 */
public record Patient(String id, String surname, LocalDateTime admitTime, int acuity)
        implements Comparable<Patient> {

    @Override
    public int compareTo(Patient other) {
        return this.surname.compareToIgnoreCase(other.surname);
    }

    @Override
    public String toString() {
        return String.format("%-8s %-15s acuity=%d", id, surname, acuity);
    }
}
