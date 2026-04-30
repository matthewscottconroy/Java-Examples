package com.ml.bayes;

import java.util.*;

/**
 * Multinomial Naive Bayes classifier for text classification.
 *
 * <p>Applies Bayes' theorem with a strong (naïve) independence assumption:
 * <pre>
 *   P(class | features) ∝ P(class) × Π P(featureᵢ | class)
 * </pre>
 *
 * <p>For text, each document is a bag of words. The model estimates:
 * <ul>
 *   <li><b>P(class)</b> — proportion of training documents in that class</li>
 *   <li><b>P(word | class)</b> — word frequency within class documents,
 *       smoothed with Laplace smoothing to handle unseen words:
 *       P(word|class) = (count + α) / (total_words_in_class + α × |vocabulary|)</li>
 * </ul>
 *
 * <p>Computation uses log-probabilities to avoid floating-point underflow
 * when multiplying many small probabilities.
 */
public class NaiveBayes {

    private final double alpha;    // Laplace smoothing parameter

    private Set<String> vocabulary;
    private Map<String, Double> logPrior;                // log P(class)
    private Map<String, Map<String, Double>> logLikelihood;  // log P(word | class)
    private List<String> classes;

    public NaiveBayes(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Trains the model.
     *
     * @param documents list of tokenised documents (each is a list of words)
     * @param labels    class label for each document
     */
    public void fit(List<List<String>> documents, List<String> labels) {
        classes = labels.stream().distinct().sorted().toList();
        vocabulary = new HashSet<>();
        for (List<String> doc : documents) vocabulary.addAll(doc);

        logPrior = new HashMap<>();
        logLikelihood = new HashMap<>();

        int totalDocs = documents.size();

        for (String cls : classes) {
            // Collect all words in documents of this class
            List<String> classWords = new ArrayList<>();
            int classDocCount = 0;
            for (int i = 0; i < documents.size(); i++) {
                if (labels.get(i).equals(cls)) {
                    classWords.addAll(documents.get(i));
                    classDocCount++;
                }
            }

            logPrior.put(cls, Math.log((double) classDocCount / totalDocs));

            // Word frequencies in this class
            Map<String, Integer> wordCount = new HashMap<>();
            for (String w : classWords) wordCount.merge(w, 1, Integer::sum);

            double totalWords = classWords.size();
            int vocabSize = vocabulary.size();

            Map<String, Double> likelihood = new HashMap<>();
            for (String word : vocabulary) {
                double count = wordCount.getOrDefault(word, 0);
                likelihood.put(word, Math.log((count + alpha) / (totalWords + alpha * vocabSize)));
            }
            // Store smoothed probability for unseen words
            likelihood.put("__UNSEEN__",
                Math.log(alpha / (totalWords + alpha * vocabSize)));

            logLikelihood.put(cls, likelihood);
        }
    }

    /** Returns the most likely class for a tokenised document. */
    public String predict(List<String> document) {
        String best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (String cls : classes) {
            double score = logPrior.get(cls);
            Map<String, Double> ll = logLikelihood.get(cls);
            for (String word : document) {
                score += ll.getOrDefault(word, ll.get("__UNSEEN__"));
            }
            if (score > bestScore) { bestScore = score; best = cls; }
        }
        return best;
    }

    /** Returns log-probability score for each class. */
    public Map<String, Double> predictLogProba(List<String> document) {
        Map<String, Double> scores = new LinkedHashMap<>();
        for (String cls : classes) {
            double score = logPrior.get(cls);
            Map<String, Double> ll = logLikelihood.get(cls);
            for (String word : document) {
                score += ll.getOrDefault(word, ll.get("__UNSEEN__"));
            }
            scores.put(cls, score);
        }
        return scores;
    }

    public double accuracy(List<List<String>> documents, List<String> labels) {
        int correct = 0;
        for (int i = 0; i < documents.size(); i++) {
            if (predict(documents.get(i)).equals(labels.get(i))) correct++;
        }
        return (double) correct / documents.size();
    }

    public Set<String> vocabulary() { return Collections.unmodifiableSet(vocabulary); }
}
