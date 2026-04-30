package com.functional.hof;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Higher-order functions for payroll processing.
 *
 * <p>A <strong>higher-order function</strong> is one that either:
 * <ul>
 *   <li>accepts another function as a parameter, or</li>
 *   <li>returns a function as its result.</li>
 * </ul>
 *
 * <p>Every method here is generic over behaviour — the caller supplies a
 * {@link Predicate} or {@link Function} that decides what "filter" or
 * "transform" means, keeping this class free of hard-coded business rules.
 */
public final class Payroll {

    private Payroll() {}

    /**
     * Filter employees by a caller-supplied predicate.
     *
     * @param employees source list
     * @param predicate a function {@code Employee → boolean}
     * @return new list containing only employees that satisfy the predicate
     */
    public static List<Employee> filter(List<Employee> employees,
                                        Predicate<Employee> predicate) {
        List<Employee> result = new ArrayList<>();
        for (Employee e : employees) {
            if (predicate.test(e)) result.add(e);
        }
        return result;
    }

    /**
     * Transform each employee into a value of type {@code R}.
     *
     * @param employees source list
     * @param mapper    a function {@code Employee → R}
     * @return new list of transformed values
     */
    public static <R> List<R> map(List<Employee> employees,
                                  Function<Employee, R> mapper) {
        List<R> result = new ArrayList<>();
        for (Employee e : employees) result.add(mapper.apply(e));
        return result;
    }

    /**
     * Sum a numeric property extracted by the given mapper.
     *
     * @param employees source list
     * @param toDouble  a function {@code Employee → double}
     * @return total of the extracted values
     */
    public static double sum(List<Employee> employees,
                             Function<Employee, Double> toDouble) {
        double total = 0;
        for (Employee e : employees) total += toDouble.apply(e);
        return total;
    }

    /**
     * Return a new predicate that is the logical AND of two predicates.
     *
     * <p>This function <em>returns a function</em> — it combines two
     * behaviours into one without executing either immediately.
     */
    public static Predicate<Employee> both(Predicate<Employee> a,
                                           Predicate<Employee> b) {
        return employee -> a.test(employee) && b.test(employee);
    }

    /**
     * Return a salary-raise function: multiply each employee's salary by
     * {@code factor} and return a new Employee record.
     *
     * <p>This function <em>returns a function</em>. The factor is baked in
     * via closure; the returned function only needs an {@code Employee}.
     */
    public static Function<Employee, Employee> raiseBy(double factor) {
        return emp -> new Employee(emp.id(), emp.name(), emp.department(),
                                   emp.salaryUsd() * factor, emp.fullTime());
    }
}
