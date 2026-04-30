package com.algorithms.dp.lcs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LCSTest {

    @Test
    @DisplayName("length: classic ABCBDAB / BDCABA → 4")
    void lcsClassic() {
        assertEquals(4, LCS.length("ABCBDAB", "BDCABA"));
    }

    @Test
    @DisplayName("length: identical strings → full length")
    void lcsIdentical() {
        assertEquals(5, LCS.length("HELLO", "HELLO"));
    }

    @Test
    @DisplayName("length: empty string → 0")
    void lcsEmpty() {
        assertEquals(0, LCS.length("", "ABC"));
        assertEquals(0, LCS.length("ABC", ""));
        assertEquals(0, LCS.length("", ""));
    }

    @Test
    @DisplayName("length: no common characters → 0")
    void lcsNoCommon() {
        assertEquals(0, LCS.length("ABC", "XYZ"));
    }

    @Test
    @DisplayName("reconstruct: result is a valid subsequence of both inputs")
    void reconstructIsSubsequence() {
        String a = "AGGTAB", b = "GXTXAYB";
        String lcs = LCS.reconstruct(a, b);
        assertTrue(isSubsequence(lcs, a), "LCS must be a subsequence of a");
        assertTrue(isSubsequence(lcs, b), "LCS must be a subsequence of b");
    }

    @Test
    @DisplayName("reconstruct: length agrees with length()")
    void reconstructLengthAgreement() {
        String a = "ABCBDAB", b = "BDCABA";
        assertEquals(LCS.length(a, b), LCS.reconstruct(a, b).length());
    }

    @Test
    @DisplayName("editDistance: same string → 0")
    void editDistanceSame() {
        assertEquals(0, LCS.editDistance("kitten", "kitten"));
    }

    @Test
    @DisplayName("editDistance: empty string → other string's length")
    void editDistanceEmpty() {
        assertEquals(5, LCS.editDistance("", "hello"));
        assertEquals(3, LCS.editDistance("cat", ""));
    }

    @Test
    @DisplayName("editDistance: classic kitten → sitting = 3")
    void editDistanceKitten() {
        assertEquals(3, LCS.editDistance("kitten", "sitting"));
    }

    @Test
    @DisplayName("editDistance: single insertion")
    void editDistanceInsertion() {
        assertEquals(1, LCS.editDistance("cat", "cats"));
    }

    @Test
    @DisplayName("editDistance: single substitution")
    void editDistanceSubstitution() {
        assertEquals(1, LCS.editDistance("cat", "bat"));
    }

    @Test
    @DisplayName("editDistance is symmetric")
    void editDistanceSymmetric() {
        assertEquals(LCS.editDistance("abc", "xyz"), LCS.editDistance("xyz", "abc"));
    }

    // Helper: checks if sub is a subsequence of s
    private static boolean isSubsequence(String sub, String s) {
        int i = 0;
        for (char c : s.toCharArray()) {
            if (i < sub.length() && c == sub.charAt(i)) i++;
        }
        return i == sub.length();
    }
}
