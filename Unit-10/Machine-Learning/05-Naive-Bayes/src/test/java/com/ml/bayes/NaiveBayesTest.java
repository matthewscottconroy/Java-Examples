package com.ml.bayes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NaiveBayesTest {

    static List<List<String>> DOCS_2CLASS = List.of(
        List.of("great", "product", "love"),
        List.of("excellent", "quality", "great"),
        List.of("terrible", "awful", "waste"),
        List.of("bad", "quality", "terrible")
    );
    static List<String> LABELS_2CLASS = List.of("positive", "positive", "negative", "negative");

    @Test
    @DisplayName("Fits without error on 2-class dataset")
    void fit_noException() {
        NaiveBayes nb = new NaiveBayes(1.0);
        assertDoesNotThrow(() -> nb.fit(DOCS_2CLASS, LABELS_2CLASS));
    }

    @Test
    @DisplayName("Vocabulary contains all unique words from training")
    void vocabulary_containsAllWords() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(DOCS_2CLASS, LABELS_2CLASS);
        assertTrue(nb.vocabulary().contains("great"));
        assertTrue(nb.vocabulary().contains("terrible"));
    }

    @Test
    @DisplayName("Positive words → predicts positive class")
    void positiveWords_predictsPositive() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(DOCS_2CLASS, LABELS_2CLASS);
        String pred = nb.predict(List.of("great", "excellent", "love"));
        assertEquals("positive", pred);
    }

    @Test
    @DisplayName("Negative words → predicts negative class")
    void negativeWords_predictsNegative() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(DOCS_2CLASS, LABELS_2CLASS);
        String pred = nb.predict(List.of("terrible", "awful", "bad"));
        assertEquals("negative", pred);
    }

    @Test
    @DisplayName("100% accuracy on training data for well-separated classes")
    void training_accuracy() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(DOCS_2CLASS, LABELS_2CLASS);
        assertEquals(1.0, nb.accuracy(DOCS_2CLASS, LABELS_2CLASS));
    }

    @Test
    @DisplayName("Unseen word does not cause exception (Laplace smoothing)")
    void unseenWord_noException() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(DOCS_2CLASS, LABELS_2CLASS);
        assertDoesNotThrow(() -> nb.predict(List.of("xyzzy", "unknown", "word")));
    }

    @Test
    @DisplayName("predictLogProba returns score for every class")
    void predictLogProba_allClasses() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(DOCS_2CLASS, LABELS_2CLASS);
        Map<String, Double> scores = nb.predictLogProba(List.of("great"));
        assertTrue(scores.containsKey("positive"));
        assertTrue(scores.containsKey("negative"));
    }

    @Test
    @DisplayName("Positive class has higher log-prob for positive text")
    void logProba_positiveText() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(DOCS_2CLASS, LABELS_2CLASS);
        Map<String, Double> scores = nb.predictLogProba(List.of("great", "excellent"));
        assertTrue(scores.get("positive") > scores.get("negative"),
            "Positive text should score higher for positive class");
    }

    @Test
    @DisplayName("3-class sentiment: ≥ 90% accuracy on training set")
    void threeClass_sentimentAccuracy() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(Main.DOCS, Main.LABELS);
        assertTrue(nb.accuracy(Main.DOCS, Main.LABELS) >= 0.9,
            "Should achieve ≥90% on training data");
    }

    @Test
    @DisplayName("Strong positive review predicts positive")
    void strongPositive_predictsPositive() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(Main.DOCS, Main.LABELS);
        String pred = nb.predict(List.of("excellent", "love", "fantastic", "great", "outstanding"));
        assertEquals("positive", pred);
    }

    @Test
    @DisplayName("Strong negative review predicts negative")
    void strongNegative_predictsNegative() {
        NaiveBayes nb = new NaiveBayes(1.0);
        nb.fit(Main.DOCS, Main.LABELS);
        String pred = nb.predict(List.of("terrible", "broken", "awful", "horrible", "waste"));
        assertEquals("negative", pred);
    }

    @Test
    @DisplayName("Alpha=0 still produces predictions (no smoothing edge case)")
    void alphaZero_stillPredicts() {
        NaiveBayes nb = new NaiveBayes(0.0);
        nb.fit(DOCS_2CLASS, LABELS_2CLASS);
        // Should still classify known words correctly
        String pred = nb.predict(List.of("great"));
        assertEquals("positive", pred);
    }
}
