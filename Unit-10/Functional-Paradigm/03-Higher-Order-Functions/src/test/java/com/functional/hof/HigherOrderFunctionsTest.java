package com.functional.hof;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class HigherOrderFunctionsTest {

    private List<Employee> staff;

    @BeforeEach
    void setUp() {
        staff = List.of(
                new Employee(1, "Alice", "Engineering", 100_000, true),
                new Employee(2, "Bob",   "Marketing",    80_000, true),
                new Employee(3, "Carol", "Engineering",  90_000, false)
        );
    }

    @Test
    @DisplayName("filter keeps only matching employees")
    void filterByDepartment() {
        List<Employee> eng = Payroll.filter(staff, e -> e.department().equals("Engineering"));
        assertEquals(2, eng.size());
        assertTrue(eng.stream().allMatch(e -> e.department().equals("Engineering")));
    }

    @Test
    @DisplayName("filter with always-false predicate returns empty list")
    void filterNone() {
        List<Employee> none = Payroll.filter(staff, e -> false);
        assertTrue(none.isEmpty());
    }

    @Test
    @DisplayName("map extracts a field from each employee")
    void mapToNames() {
        List<String> names = Payroll.map(staff, Employee::name);
        assertEquals(List.of("Alice", "Bob", "Carol"), names);
    }

    @Test
    @DisplayName("sum totals a numeric property")
    void sumSalaries() {
        double total = Payroll.sum(staff, e -> e.salaryUsd());
        assertEquals(270_000.0, total, 0.01);
    }

    @Test
    @DisplayName("both combines two predicates with AND")
    void bothPredicates() {
        Predicate<Employee> isEng      = e -> e.department().equals("Engineering");
        Predicate<Employee> isFullTime = Employee::fullTime;
        List<Employee> result = Payroll.filter(staff, Payroll.both(isEng, isFullTime));
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).name());
    }

    @Test
    @DisplayName("raiseBy returns a function that scales salary")
    void raiseByTenPercent() {
        Function<Employee, Employee> raise = Payroll.raiseBy(1.10);
        Employee raised = raise.apply(new Employee(1, "Alice", "Eng", 100_000, true));
        assertEquals(110_000.0, raised.salaryUsd(), 0.01);
    }

    @Test
    @DisplayName("raiseBy does not mutate the original employee")
    void raiseByIsImmutable() {
        Employee original = new Employee(1, "Alice", "Eng", 100_000, true);
        Payroll.raiseBy(1.20).apply(original);
        assertEquals(100_000.0, original.salaryUsd());
    }
}
