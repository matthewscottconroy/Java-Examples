package com.patterns.prototype;

import java.util.ArrayList;
import java.util.List;

/**
 * Prototype — a legal contract that can clone itself.
 *
 * <p>The law firm maintains a small library of master templates: NDA, Service
 * Agreement, Employment Contract. When a new client engagement starts, a lawyer
 * calls {@link #clone()} to get a personal copy, then fills in the client-specific
 * details. The master template is never modified.
 *
 * <p>Using {@code clone()} avoids the expensive alternative of constructing a
 * new contract from scratch and re-populating all the boilerplate clauses.
 *
 * <p><b>Pattern role:</b> this class is both the {@code Prototype} interface
 * and a concrete implementation, since Java's {@code Cloneable} mechanism
 * serves as the marker interface.
 */
public class Contract implements Cloneable {

    private String type;
    private String partyA;
    private String partyB;
    private String effectiveDate;
    private final List<String> clauses;

    /**
     * Creates a master contract template.
     *
     * @param type   the contract type (e.g., "Non-Disclosure Agreement")
     */
    public Contract(String type) {
        this.type    = type;
        this.partyA  = "[PARTY A]";
        this.partyB  = "[PARTY B]";
        this.effectiveDate = "[DATE]";
        this.clauses = new ArrayList<>();
    }

    /** Copy constructor used by {@link #clone()}. */
    private Contract(Contract source) {
        this.type          = source.type;
        this.partyA        = source.partyA;
        this.partyB        = source.partyB;
        this.effectiveDate = source.effectiveDate;
        this.clauses       = new ArrayList<>(source.clauses); // deep copy
    }

    /**
     * Adds a standard clause to this contract.
     *
     * @param clause the clause text
     * @return this contract (for chaining when building templates)
     */
    public Contract addClause(String clause) {
        clauses.add(clause);
        return this;
    }

    /**
     * Creates an independent copy of this contract.
     *
     * <p>The clone has all the same clauses as the original but independent
     * state — modifying the clone's party names or adding clauses does not
     * affect the master template.
     *
     * @return a deep copy of this contract
     */
    @Override
    public Contract clone() {
        return new Contract(this);
    }

    /**
     * Fills in the client-specific fields on a cloned contract.
     *
     * @param partyA        the first party's name
     * @param partyB        the second party's name
     * @param effectiveDate the date the contract takes effect
     * @return this contract (for chaining)
     */
    public Contract customise(String partyA, String partyB, String effectiveDate) {
        this.partyA        = partyA;
        this.partyB        = partyB;
        this.effectiveDate = effectiveDate;
        return this;
    }

    /** @return the contract type */
    public String getType()          { return type; }

    /** @return party A's name */
    public String getPartyA()        { return partyA; }

    /** @return party B's name */
    public String getPartyB()        { return partyB; }

    /** @return the effective date string */
    public String getEffectiveDate() { return effectiveDate; }

    /** @return the number of clauses in this contract */
    public int getClauseCount()      { return clauses.size(); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(type).append(" ===\n");
        sb.append("Between: ").append(partyA).append(" and ").append(partyB).append("\n");
        sb.append("Effective: ").append(effectiveDate).append("\n");
        sb.append("Clauses (").append(clauses.size()).append("):\n");
        clauses.forEach(c -> sb.append("  • ").append(c).append("\n"));
        return sb.toString();
    }
}
