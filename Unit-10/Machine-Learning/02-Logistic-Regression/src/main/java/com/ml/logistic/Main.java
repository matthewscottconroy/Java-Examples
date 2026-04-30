package com.ml.logistic;

/**
 * Email spam classifier.
 *
 * Features (all normalised to [0,1]):
 *   [0] exclamation_mark_ratio   — proportion of '!' characters
 *   [1] uppercase_ratio          — proportion of uppercase characters
 *   [2] link_count_norm          — number of hyperlinks, scaled
 *   [3] word_count_norm          — email length, scaled
 *   [4] known_spam_word_count    — count of flagged words, scaled
 *
 * Label: 1 = spam, 0 = not spam
 */
public class Main {

    // [exclamation, uppercase, links, length, spam_words]
    static final double[][] X = {
        { 0.8, 0.7, 0.9, 0.3, 0.9 },  // spam
        { 0.0, 0.1, 0.0, 0.8, 0.0 },  // ham
        { 0.6, 0.8, 0.7, 0.4, 0.8 },  // spam
        { 0.1, 0.2, 0.1, 0.6, 0.1 },  // ham
        { 0.9, 0.9, 0.8, 0.2, 1.0 },  // spam
        { 0.0, 0.1, 0.0, 0.9, 0.0 },  // ham
        { 0.7, 0.6, 0.8, 0.3, 0.7 },  // spam
        { 0.1, 0.3, 0.0, 0.7, 0.0 },  // ham
        { 0.5, 0.5, 0.6, 0.4, 0.6 },  // spam
        { 0.0, 0.2, 0.1, 0.8, 0.1 },  // ham
        { 0.8, 0.7, 0.9, 0.2, 0.9 },  // spam
        { 0.2, 0.1, 0.0, 0.7, 0.0 },  // ham
        { 0.6, 0.8, 0.7, 0.5, 0.8 },  // spam
        { 0.0, 0.2, 0.0, 0.6, 0.0 },  // ham
        { 0.9, 0.9, 1.0, 0.1, 1.0 },  // spam
        { 0.1, 0.1, 0.0, 0.9, 0.0 },  // ham
        { 0.7, 0.7, 0.8, 0.3, 0.7 },  // spam
        { 0.0, 0.3, 0.1, 0.8, 0.1 },  // ham
        { 0.5, 0.6, 0.5, 0.4, 0.5 },  // spam
        { 0.1, 0.1, 0.0, 0.9, 0.0 },  // ham
    };

    static final int[] Y = { 1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0 };

    public static void main(String[] args) {
        System.out.println("=== Logistic Regression — Spam Classifier ===\n");

        LogisticRegression model = new LogisticRegression(0.5, 1000);
        model.fit(X, Y);

        System.out.printf("Training accuracy: %.1f%%%n", model.accuracy(X, Y) * 100);

        System.out.println("\nSample predictions:");
        System.out.printf("  %-40s  %-8s  %-8s  %s%n", "Email features", "Actual", "Predicted", "Prob(spam)");
        System.out.println("  " + "-".repeat(72));

        String[] labels = { "High excl+upper+links+spam_words", "Low excl, long normal email", "Medium signals" };
        int[] idxs = { 0, 1, 8 };
        for (int i = 0; i < idxs.length; i++) {
            int idx = idxs[i];
            double prob = model.predictProba(X[idx]);
            int pred = model.predict(X[idx]);
            System.out.printf("  %-40s  %-8s  %-8s  %.3f%n",
                labels[i],
                Y[idx] == 1 ? "SPAM" : "HAM",
                pred == 1 ? "SPAM" : "HAM",
                prob);
        }

        // Unseen email
        System.out.println("\nUnseen email (moderate signals: excl=0.4, upper=0.5, links=0.3, len=0.6, spam=0.4):");
        double[] newEmail = { 0.4, 0.5, 0.3, 0.6, 0.4 };
        double prob = model.predictProba(newEmail);
        System.out.printf("  P(spam) = %.3f  →  %s%n", prob, prob >= 0.5 ? "SPAM" : "HAM");
    }
}
