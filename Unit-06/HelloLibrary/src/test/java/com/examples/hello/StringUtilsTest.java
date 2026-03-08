package com.examples.hello;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringUtils")
class StringUtilsTest {

    @Test
    @DisplayName("repeat: repeats a string the given number of times")
    void repeatWorks() {
        assertEquals("abcabcabc", StringUtils.repeat("abc", 3));
    }

    @Test
    @DisplayName("repeat: zero times returns empty string")
    void repeatZeroTimesIsEmpty() {
        assertEquals("", StringUtils.repeat("abc", 0));
    }

    @Test
    @DisplayName("repeat: negative count throws IllegalArgumentException")
    void repeatNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> StringUtils.repeat("abc", -1));
    }

    @Test
    @DisplayName("isPalindrome: 'racecar' is a palindrome")
    void racecarIsPalindrome() {
        assertTrue(StringUtils.isPalindrome("racecar"));
    }

    @Test
    @DisplayName("isPalindrome: ignores spaces and punctuation")
    void palindromeIgnoresPunctuation() {
        assertTrue(StringUtils.isPalindrome("A man, a plan, a canal: Panama"));
    }

    @Test
    @DisplayName("isPalindrome: 'hello' is not a palindrome")
    void helloIsNotPalindrome() {
        assertFalse(StringUtils.isPalindrome("hello"));
    }

    @Test
    @DisplayName("toTitleCase: capitalises each word")
    void toTitleCaseWorks() {
        assertEquals("The Quick Brown Fox", StringUtils.toTitleCase("the quick brown fox"));
    }
}
