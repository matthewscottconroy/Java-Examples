package com.patterns.composite;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite — a named group that contains other {@link Employee} nodes.
 *
 * <p>A {@code Team} can hold individual {@link Staff} (leaves) or other
 * {@code Team}s (composites). Every operation delegates to all children,
 * producing a recursive result. The caller never checks whether a node is
 * a leaf or a composite — it calls the same method either way.
 */
public class Team implements Employee {

    private final String         name;
    private final List<Employee> members = new ArrayList<>();

    /**
     * @param name the team or department name
     */
    public Team(String name) {
        this.name = name;
    }

    /**
     * Adds a member to this team (may be a {@link Staff} or another {@code Team}).
     *
     * @param employee the employee or sub-team to add
     * @return this team (for chaining)
     */
    public Team add(Employee employee) {
        members.add(employee);
        return this;
    }

    @Override
    public String getName() { return name; }

    /** Sums salaries of all direct and indirect members. */
    @Override
    public long getTotalSalary() {
        return members.stream().mapToLong(Employee::getTotalSalary).sum();
    }

    /** Applies the raise to every member, recursively. */
    @Override
    public void applyRaise(double percent) {
        members.forEach(m -> m.applyRaise(percent));
    }

    @Override
    public void printStructure(int indent) {
        System.out.printf("%" + (indent > 0 ? indent : 1) + "s[%s] (total: $%,d/yr)%n",
                " ", name, getTotalSalary());
        members.forEach(m -> m.printStructure(indent + 4));
    }
}
