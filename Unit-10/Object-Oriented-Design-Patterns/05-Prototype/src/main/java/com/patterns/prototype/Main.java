package com.patterns.prototype;

/**
 * Demonstrates the Prototype pattern using a legal contract library.
 *
 * <p>The law firm sets up master templates once. Each new engagement clones a
 * template and customises only the client-specific fields. The master template
 * remains unchanged throughout.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Legal Contract Templates (Prototype Pattern) ===\n");

        // --- Build master templates once ---
        ContractLibrary library = new ContractLibrary();

        Contract ndaTemplate = new Contract("Non-Disclosure Agreement")
                .addClause("Confidential information shall not be disclosed to third parties.")
                .addClause("This agreement is binding for a period of two (2) years.")
                .addClause("Violating this agreement shall result in damages of $500,000.");

        Contract serviceTemplate = new Contract("Service Agreement")
                .addClause("Provider shall deliver services as described in Schedule A.")
                .addClause("Payment is due within 30 days of invoice.")
                .addClause("Either party may terminate with 30 days written notice.");

        library.register("NDA", ndaTemplate);
        library.register("SERVICE", serviceTemplate);

        // --- Clone and customise for each engagement ---
        System.out.println("--- Engagement 1: NDA between Acme Corp and Beta LLC ---");
        Contract acmeBetaNda = library.get("NDA")
                .customise("Acme Corp", "Beta LLC", "2026-05-01");
        System.out.println(acmeBetaNda);

        System.out.println("--- Engagement 2: NDA between GlobalTech and Innovate Inc ---");
        Contract globalNda = library.get("NDA")
                .customise("GlobalTech Inc.", "Innovate Inc.", "2026-06-15");
        globalNda.addClause("Jurisdiction: State of California.");
        System.out.println(globalNda);

        System.out.println("--- Engagement 3: Service Agreement ---");
        Contract serviceContract = library.get("SERVICE")
                .customise("Devhaus Studio", "MegaBank Corp", "2026-07-01");
        System.out.println(serviceContract);

        // Prove the master NDA template is unchanged (still has [PARTY A])
        System.out.println("--- Master NDA template is unchanged ---");
        System.out.println("Master partyA = " + ndaTemplate.getPartyA()
                + ", clauses = " + ndaTemplate.getClauseCount());
    }
}
