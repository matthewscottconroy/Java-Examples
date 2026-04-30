package com.algorithms.sorting.mergesort;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Patient Record Merger (Merge Sort) ===\n");

        // Each hospital already has its patients sorted by surname
        List<Patient> northHospital = List.of(
                new Patient("N001", "Abbas",    LocalDateTime.of(2024,6,1,8,0),  3),
                new Patient("N002", "Chen",     LocalDateTime.of(2024,6,1,8,30), 2),
                new Patient("N003", "Garcia",   LocalDateTime.of(2024,6,1,9,0),  4),
                new Patient("N004", "Morrison", LocalDateTime.of(2024,6,1,7,45), 1)
        );
        List<Patient> southHospital = List.of(
                new Patient("S001", "Brown",    LocalDateTime.of(2024,6,1,8,15), 3),
                new Patient("S002", "Kim",      LocalDateTime.of(2024,6,1,9,30), 5),
                new Patient("S003", "Patel",    LocalDateTime.of(2024,6,1,8,45), 2),
                new Patient("S004", "Torres",   LocalDateTime.of(2024,6,1,7,30), 1)
        );

        System.out.println("North Hospital (pre-sorted by surname):");
        northHospital.forEach(p -> System.out.println("  " + p));

        System.out.println("\nSouth Hospital (pre-sorted by surname):");
        southHospital.forEach(p -> System.out.println("  " + p));

        // Merge O(n) — exploits the fact both lists are already sorted
        List<Patient> merged = MergeSort.merge(northHospital, southHospital,
                Comparator.naturalOrder());
        System.out.println("\nMerged combined list (no re-sort needed):");
        merged.forEach(p -> System.out.println("  " + p));

        // Full sort of a jumbled emergency queue by acuity (1=critical first)
        List<Patient> queue = List.of(
                new Patient("E001", "Walsh",   LocalDateTime.now(), 4),
                new Patient("E002", "Ahmed",   LocalDateTime.now(), 1),
                new Patient("E003", "Yuen",    LocalDateTime.now(), 3),
                new Patient("E004", "Dubois",  LocalDateTime.now(), 2),
                new Patient("E005", "Nakamura",LocalDateTime.now(), 5),
                new Patient("E006", "O'Brien", LocalDateTime.now(), 1)
        );
        List<Patient> byAcuity = MergeSort.sort(queue,
                Comparator.comparingInt(Patient::acuity)
                          .thenComparing(Patient::surname));
        System.out.println("\nEmergency queue sorted by acuity (1=critical):");
        byAcuity.forEach(p -> System.out.println("  " + p));
    }
}
