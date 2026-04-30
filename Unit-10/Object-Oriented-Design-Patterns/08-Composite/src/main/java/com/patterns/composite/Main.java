package com.patterns.composite;

/**
 * Demonstrates the Composite pattern with a company org chart.
 *
 * <p>The company tree is built from individuals ({@link Staff}) nested inside
 * teams ({@link Team}). Every operation — printing the structure, computing
 * total payroll, applying a company-wide raise — works identically at any level
 * of the hierarchy, with no type-checking or special cases.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Company Org Chart (Composite Pattern) ===\n");

        // Individual contributors
        Staff alice   = new Staff("Alice",   "Senior Engineer",     130_000);
        Staff bob     = new Staff("Bob",     "Engineer",             95_000);
        Staff carol   = new Staff("Carol",   "Engineer",             95_000);
        Staff dave    = new Staff("Dave",    "UX Designer",          90_000);
        Staff eve     = new Staff("Eve",     "Product Manager",     115_000);
        Staff frank   = new Staff("Frank",   "Sales Rep",            75_000);
        Staff grace   = new Staff("Grace",   "Sales Manager",       100_000);
        Staff hank    = new Staff("Hank",    "CEO",                 250_000);

        // Build the tree
        Team engineering = new Team("Engineering").add(alice).add(bob).add(carol);
        Team product     = new Team("Product").add(dave).add(eve);
        Team sales       = new Team("Sales").add(frank).add(grace);
        Team technology  = new Team("Technology Division").add(engineering).add(product);
        Team company     = new Team("Acme Corp").add(hank).add(technology).add(sales);

        // Print structure — same call works on the whole company or a single team
        System.out.println("=== Full Org Chart ===");
        company.printStructure(1);

        System.out.printf("%nTotal company payroll: $%,d/yr%n", company.getTotalSalary());
        System.out.printf("Engineering team payroll: $%,d/yr%n%n", engineering.getTotalSalary());

        // Apply a 10% raise to the entire company in one call
        System.out.println("Applying 10% company-wide raise...");
        company.applyRaise(10);
        System.out.printf("New total payroll: $%,d/yr%n%n", company.getTotalSalary());

        // The raise propagated to Alice even though we called it on 'company'
        System.out.printf("Alice's new salary: $%,d/yr%n", alice.getTotalSalary());
    }
}
