package com.patterns.composite;

/**
 * Leaf — an individual employee with a single salary.
 *
 * <p>Has no children. Every operation is performed on this person alone.
 */
public class Staff implements Employee {

    private final String name;
    private final String title;
    private long salary;

    /**
     * @param name   the employee's full name
     * @param title  the employee's job title
     * @param salary the annual salary in dollars
     */
    public Staff(String name, String title, long salary) {
        this.name   = name;
        this.title  = title;
        this.salary = salary;
    }

    @Override
    public String getName()         { return name; }

    @Override
    public long getTotalSalary()    { return salary; }

    @Override
    public void applyRaise(double percent) {
        salary = Math.round(salary * (1 + percent / 100.0));
    }

    @Override
    public void printStructure(int indent) {
        System.out.printf("%" + (indent > 0 ? indent : 1) + "s%s — %s ($%,d/yr)%n",
                " ", name, title, salary);
    }
}
