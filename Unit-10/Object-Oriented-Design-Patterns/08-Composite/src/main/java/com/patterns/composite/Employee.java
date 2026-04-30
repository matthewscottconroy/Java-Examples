package com.patterns.composite;

/**
 * Component — the common interface for both individual staff and groups of staff.
 *
 * <p>Because both leaves ({@link Staff}) and composites ({@link Team}) implement
 * this interface, client code can call {@code getTotalSalary()} or
 * {@code printStructure()} on a single person or an entire division — identically.
 * The client never needs to distinguish the two cases.
 */
public interface Employee {

    /** @return the display name of this employee or group */
    String getName();

    /**
     * Returns the total annual salary of this employee or all employees
     * in this group, recursively.
     *
     * @return total annual salary in dollars
     */
    long getTotalSalary();

    /**
     * Prints the employee or the team tree, indented to the given depth.
     *
     * @param indent the number of spaces to prefix (increases with nesting)
     */
    void printStructure(int indent);

    /**
     * Applies a percentage raise to this employee or to all employees in
     * this group, recursively.
     *
     * @param percent the raise percentage (e.g., 10 means 10%)
     */
    void applyRaise(double percent);
}
