package com.meta.recursive;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Recursive Structures ===\n");

        // ---------------------------------------------------------------
        // FList — functional linked list
        // ---------------------------------------------------------------
        System.out.println("--- FList ---");
        FList<Integer> nums = FList.of(1, 2, 3, 4, 5);
        System.out.println("Original:           " + nums.show());
        System.out.println("map(x -> x*2):      " + nums.map(x -> x * 2).show());
        System.out.println("filter(x > 2):      " + nums.filter(x -> x > 2).show());
        System.out.println("foldRight(+, 0):    " + nums.foldRight(Integer::sum, 0));
        System.out.println("reverse:            " + nums.reverse().show());
        System.out.println("size:               " + nums.size());

        // List sharing — prepend reuses the tail without copying
        FList<Integer> extended = nums.prepend(0);
        System.out.println("prepend(0):         " + extended.show());
        System.out.println("Original unchanged: " + nums.show());

        // flatMap: each element fans out into a list
        FList<Integer> expanded = FList.of(1, 2, 3)
            .flatMap(x -> FList.of(x, x * 10));
        System.out.println("flatMap([x, x*10]): " + expanded.show());

        // ---------------------------------------------------------------
        // BTree — immutable binary search tree
        // ---------------------------------------------------------------
        System.out.println("\n--- BTree ---");
        BTree<Integer> tree = BTree.of(5, 3, 7, 1, 4, 6, 9, 2, 8);
        System.out.println("Size:              " + tree.size());
        System.out.println("Height:            " + tree.height());
        System.out.println("Min:               " + tree.min());
        System.out.println("Max:               " + tree.max());
        System.out.println("Contains 4:        " + tree.contains(4));
        System.out.println("Contains 10:       " + tree.contains(10));
        System.out.println("In-order (sorted): " + tree.inOrder().show());

        BTree<Integer> doubled = tree.map(x -> x * 2);
        System.out.println("Doubled in-order:  " + doubled.inOrder().show());

        int sum = tree.fold((v, leftSum, rightSum) -> v + leftSum + rightSum, 0);
        System.out.println("Sum via fold:      " + sum);

        // ---------------------------------------------------------------
        // Structural recursion as a design principle
        // ---------------------------------------------------------------
        System.out.println("\n--- Word frequency ---");
        FList<String> words = FList.of("the", "cat", "sat", "on", "the", "mat", "the");
        long theCount = words.filter(w -> w.equals("the")).foldLeft((acc, w) -> acc + 1, 0L);
        System.out.println("Occurrences of 'the': " + theCount);
        FList<String> unique = words.foldLeft(
            (acc, w) -> acc.contains(w) ? acc : acc.prepend(w),
            FList.<String>nil()
        ).reverse();
        System.out.println("Unique words: " + unique.show());
    }
}
