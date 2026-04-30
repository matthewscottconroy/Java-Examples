package com.functional.hof;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Demonstrates higher-order functions with a payroll processing system.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Payroll Processor (Higher-Order Functions) ===\n");

        List<Employee> staff = List.of(
                new Employee(1, "Alice Chen",   "Engineering", 95_000, true),
                new Employee(2, "Bob Patel",    "Marketing",   72_000, true),
                new Employee(3, "Carol James",  "Engineering", 105_000, true),
                new Employee(4, "Dave Nguyen",  "HR",          68_000, false),
                new Employee(5, "Eve Torres",   "Engineering", 88_000, false),
                new Employee(6, "Frank Liu",    "Marketing",   76_000, true)
        );

        // filter: a function passed in to decide what to keep
        Predicate<Employee> isEngineering = e -> e.department().equals("Engineering");
        Predicate<Employee> isFullTime    = Employee::fullTime;

        List<Employee> engFullTime = Payroll.filter(staff, Payroll.both(isEngineering, isFullTime));
        System.out.println("Full-time Engineering employees:");
        engFullTime.forEach(e -> System.out.printf("  %-15s $%,.0f%n", e.name(), e.salaryUsd()));

        // map: transform each employee into their name
        List<String> names = Payroll.map(staff, Employee::name);
        System.out.println("\nAll names: " + names);

        // sum: extract salary from each employee and total it
        double totalPayroll = Payroll.sum(staff, e -> e.salaryUsd());
        System.out.printf("%nTotal payroll: $%,.0f%n", totalPayroll);

        // raiseBy returns a function — apply it with map
        Function<Employee, Employee> tenPercentRaise = Payroll.raiseBy(1.10);
        List<Employee> raised = Payroll.map(staff, tenPercentRaise);
        double newTotal = Payroll.sum(raised, e -> e.salaryUsd());
        System.out.printf("Payroll after 10%% raise: $%,.0f%n", newTotal);

        // Chain: filter to full-time, then raise, then sum
        List<Employee> fullTime = Payroll.filter(staff, isFullTime);
        double fullTimeCost = Payroll.sum(Payroll.map(fullTime, tenPercentRaise), e -> e.salaryUsd());
        System.out.printf("Full-time cost after raise: $%,.0f%n", fullTimeCost);
    }
}
