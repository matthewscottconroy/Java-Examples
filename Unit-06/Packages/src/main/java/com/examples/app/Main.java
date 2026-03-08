package com.examples.app;

import com.examples.format.AnsiColor;
import com.examples.format.ArrangementRenderer;
import com.examples.math.Arrangement;
import com.examples.math.Permutation;
import com.examples.math.StructureMap;

import java.util.List;
import java.util.Map;

/**
 * The Talent Show Lineup Problem.
 *
 * <p>Four performers need to be ordered for a show. We count the possibilities,
 * compose permutations to reach a desired lineup, undo a shuffle, and then
 * reveal that "ordering performers" is structurally identical to "ordering
 * numbers" — the isomorphism at the heart of permutation theory.
 */
public final class Main {

    public static void main(String[] args) {

        // -----------------------------------------------------------------------
        // Setup
        // -----------------------------------------------------------------------
        Arrangement<String> original = Arrangement.of("Alice", "Bob", "Carol", "Dave");

        printBanner();

        // -----------------------------------------------------------------------
        // 1. Count the orderings
        // -----------------------------------------------------------------------
        ArrangementRenderer.printHeader("How many lineups are there?");

        ArrangementRenderer.info("Our performers:");
        ArrangementRenderer.renderArrangement("Current lineup:", original, "Spot");

        long count = original.countOrderings();
        ArrangementRenderer.result("With " + original.size() + " performers, there are "
                + original.size() + "! = " + count + " possible lineups.");

        // -----------------------------------------------------------------------
        // 2. Apply individual permutations
        // -----------------------------------------------------------------------
        ArrangementRenderer.printHeader("Permutations — Named Shuffles");

        Permutation rotateLeft = Permutation.rotation(4, 1);
        Arrangement<String> rotated = original.permute(rotateLeft);
        ArrangementRenderer.renderTransformation(
                "Rotate everyone left by one spot:", original, rotateLeft, rotated);

        System.out.println();
        ArrangementRenderer.renderPermutation("Details:", rotateLeft);

        System.out.println();
        Permutation swapFirstTwo = Permutation.swap(4, 0, 1);
        Arrangement<String> swapped = original.permute(swapFirstTwo);
        ArrangementRenderer.renderTransformation(
                "Swap the first two performers:", original, swapFirstTwo, swapped);

        System.out.println();
        ArrangementRenderer.renderPermutation("Details:", swapFirstTwo);

        // -----------------------------------------------------------------------
        // 3. Compose permutations
        // -----------------------------------------------------------------------
        ArrangementRenderer.printHeader("Composing Permutations");

        ArrangementRenderer.info("The director's dream lineup: [Dave, Alice, Carol, Bob]");
        Arrangement<String> goal = Arrangement.of("Dave", "Alice", "Carol", "Bob");
        ArrangementRenderer.renderArrangement("Goal:", goal, "Spot");

        ArrangementRenderer.info("Can we build this out of simpler moves?");

        // rotate left by 3 == rotate right by 1
        Permutation step1 = Permutation.rotation(4, 3);
        Arrangement<String> afterStep1 = original.permute(step1);
        ArrangementRenderer.renderTransformation(
                "Step 1 — Rotate left by 3:", original, step1, afterStep1);

        System.out.println();
        Permutation step2 = Permutation.swap(4, 2, 3);
        Arrangement<String> afterStep2 = afterStep1.permute(step2);
        ArrangementRenderer.renderTransformation(
                "Step 2 — Swap spots 3 and 4:", afterStep1, step2, afterStep2);

        System.out.println();
        Permutation combined = step1.thenApply(step2);
        ArrangementRenderer.info("Combined permutation: " + combined + "  " + combined.toCycleNotation());

        boolean reachedGoal = afterStep2.equals(goal);
        ArrangementRenderer.result("Reached the goal? " + (reachedGoal ? "YES" : "NO"));

        // -----------------------------------------------------------------------
        // 4. Inverting — undoing a shuffle
        // -----------------------------------------------------------------------
        ArrangementRenderer.printHeader("The Inverse — Undoing a Shuffle");

        ArrangementRenderer.info("Suppose we only have the director's lineup and want to recover the original.");
        ArrangementRenderer.renderArrangement("We start with:", goal, "Spot");

        Permutation undo = combined.inverse();
        Arrangement<String> recovered = goal.permute(undo);
        ArrangementRenderer.renderTransformation(
                "Apply the inverse permutation:", goal, undo, recovered);

        ArrangementRenderer.result("Got back the original? " + recovered.equals(original));

        // -----------------------------------------------------------------------
        // 5. permutationTo — derive the permutation between two arrangements
        // -----------------------------------------------------------------------
        ArrangementRenderer.printHeader("Finding the Permutation Between Two Arrangements");

        ArrangementRenderer.info("Given two arrangements, what permutation connects them?");
        Arrangement<String> currentShow  = Arrangement.of("Carol", "Dave", "Alice", "Bob");
        Arrangement<String> desiredShow  = Arrangement.of("Alice", "Bob", "Carol", "Dave");
        ArrangementRenderer.renderArrangement("Current show:  ", currentShow, "Spot");
        ArrangementRenderer.renderArrangement("Desired show:  ", desiredShow, "Spot");

        Permutation bridge = currentShow.permutationTo(desiredShow);
        ArrangementRenderer.info("Permutation needed: " + bridge + "  " + bridge.toCycleNotation());
        ArrangementRenderer.renderArrangement("After applying it:", currentShow.permute(bridge), "Spot");
        ArrangementRenderer.result("Matches desired? " + currentShow.permute(bridge).equals(desiredShow));

        // -----------------------------------------------------------------------
        // 6. The structure map — isomorphism
        // -----------------------------------------------------------------------
        ArrangementRenderer.printHeader("The Structure Map — Isomorphism");

        ArrangementRenderer.info("Performers can be labeled by their original position index:");
        System.out.println();

        // Map names to indices via the canonical isomorphism
        Map<String, Integer> seatNumber = Map.of(
                "Alice", 1, "Bob", 2, "Carol", 3, "Dave", 4);
        StructureMap<String, Integer> iso = new StructureMap<>(original, seatNumber::get);

        ArrangementRenderer.renderArrangement("Performers:", original, "Spot");
        ArrangementRenderer.renderArrangement("As numbers: ", iso.applyToSource(), "Spot");

        ArrangementRenderer.printSubHeader("Verifying the isomorphism property");
        ArrangementRenderer.info(
                "Claim: for any shuffle, relabeling before or after gives the same result.");
        System.out.println();

        List<Permutation> testPerms = List.of(
                Permutation.swap(4, 0, 1),
                Permutation.rotation(4, 1),
                combined
        );
        List<String> permLabels = List.of(
                "Swap spots 0,1",
                "Rotate left by 1",
                "Director's combined move"
        );
        for (int i = 0; i < testPerms.size(); i++) {
            ArrangementRenderer.renderIsomorphismCheck(permLabels.get(i), iso, testPerms.get(i));
            System.out.println();
        }

        boolean allGood = iso.isValidIsomorphism();
        ArrangementRenderer.result("Full isomorphism check (all swaps + rotations): "
                + (allGood ? "PASSED" : "FAILED"));

        // -----------------------------------------------------------------------
        // 7. Conclusion
        // -----------------------------------------------------------------------
        ArrangementRenderer.printHeader("Conclusion");

        ArrangementRenderer.line(AnsiColor.BOLD + "What we learned:" + AnsiColor.RESET);
        System.out.println();
        ArrangementRenderer.info("4 performers  =>  4! = 24 possible lineups");
        ArrangementRenderer.info("Every lineup-change is a permutation (a bijection on positions)");
        ArrangementRenderer.info("Permutations compose: combine two moves into one");
        ArrangementRenderer.info("Every permutation has an inverse: undo any shuffle");
        ArrangementRenderer.info("Any two n-element sets have the same shuffle structure");
        ArrangementRenderer.info("That last point is what 'isomorphism' means!");
        System.out.println();
        ArrangementRenderer.result("The symmetric group S_" + original.size()
                + " has " + original.countOrderings() + " elements. "
                + "It doesn't care whether you're shuffling performers or numbers.");
        System.out.println();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(AnsiColor.BOLD + AnsiColor.BRIGHT_MAGENTA
                + "  ╔══════════════════════════════════════════════════╗"
                + AnsiColor.RESET);
        System.out.println(AnsiColor.BOLD + AnsiColor.BRIGHT_MAGENTA
                + "  ║       THE TALENT SHOW LINEUP PROBLEM            ║"
                + AnsiColor.RESET);
        System.out.println(AnsiColor.BOLD + AnsiColor.BRIGHT_MAGENTA
                + "  ║   Permutation Theory, One Shuffle at a Time     ║"
                + AnsiColor.RESET);
        System.out.println(AnsiColor.BOLD + AnsiColor.BRIGHT_MAGENTA
                + "  ╚══════════════════════════════════════════════════╝"
                + AnsiColor.RESET);
    }
}
