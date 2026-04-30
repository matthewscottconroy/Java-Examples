package com.ml.bayes;

import java.util.*;

/**
 * Sentiment analyser for product reviews.
 *
 * Documents are short reviews tokenised into words.
 * Classes: "positive", "negative", "neutral"
 */
public class Main {

    static List<List<String>> DOCS;
    static List<String> LABELS;

    static {
        DOCS = new ArrayList<>();
        LABELS = new ArrayList<>();

        // Positive reviews
        add("great product love it works perfectly",         "positive");
        add("amazing quality fantastic value highly recommend", "positive");
        add("excellent fast delivery perfect condition",     "positive");
        add("love this product best purchase ever made",     "positive");
        add("outstanding quality wonderful experience",      "positive");
        add("superb product works great very happy",         "positive");

        // Negative reviews
        add("terrible quality broke immediately waste money",  "negative");
        add("awful product never works poor quality avoid",    "negative");
        add("horrible experience worst purchase regret it",    "negative");
        add("broken on arrival terrible customer service",     "negative");
        add("disappointing poor build quality not recommend",  "negative");
        add("useless product terrible value waste time money", "negative");

        // Neutral reviews
        add("product okay average quality as expected",      "neutral");
        add("decent works fine nothing special",             "neutral");
        add("average product does job not great not bad",    "neutral");
        add("acceptable quality reasonable price okay",      "neutral");
        add("mediocre product works sometimes okay",         "neutral");
        add("ordinary product meets expectations fine",      "neutral");
    }

    static void add(String text, String label) {
        DOCS.add(List.of(text.split(" ")));
        LABELS.add(label);
    }

    public static void main(String[] args) {
        System.out.println("=== Naive Bayes — Sentiment Analyser ===\n");

        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(DOCS, LABELS);

        System.out.printf("Vocabulary size:   %d words%n", nb.vocabulary().size());
        System.out.printf("Training accuracy: %.1f%%%n", nb.accuracy(DOCS, LABELS) * 100);

        System.out.println("\nSample classifications:");
        System.out.printf("  %-50s  %-10s  %-10s%n", "Review", "Actual", "Predicted");
        System.out.println("  " + "-".repeat(73));
        int[] idxs = { 0, 6, 12 };
        for (int idx : idxs) {
            String text = String.join(" ", DOCS.get(idx));
            String pred = nb.predict(DOCS.get(idx));
            System.out.printf("  %-50s  %-10s  %-10s%n",
                text.length() > 48 ? text.substring(0, 48) + ".." : text,
                LABELS.get(idx), pred);
        }

        System.out.println("\nClassifying unseen reviews:");
        classify(nb, "excellent product love it highly recommend");
        classify(nb, "terrible broke immediately waste of money");
        classify(nb, "product is okay does the job nothing special");
        classify(nb, "amazing excellent love fantastic superb outstanding");
    }

    static void classify(NaiveBayes nb, String text) {
        List<String> doc = List.of(text.split(" "));
        String pred = nb.predict(doc);
        System.out.printf("  \"%s\"%n    → %s%n", text, pred);
    }
}
