public class CrosswordWordSearch {
    public static void main(String[] args) {
        char[][] grid = {
                "CATSX".toCharArray(),
                "AOOOX".toCharArray(),
                "TPIGS".toCharArray(),
                "SEALX".toCharArray(),
                "XXXXX".toCharArray()
        };

        String[] words = {"CATS", "PIG", "SEAL", "DOG"};

        for (String w : words) {
            boolean found = containsWord(grid, w);
            System.out.printf("%-5s -> %s%n", w, found ? "FOUND" : "not found");
        }
    }

    static boolean containsWord(char[][] g, String word) {
        return findHorizontal(g, word) || findVertical(g, word);
    }

    static boolean findHorizontal(char[][] g, String word) {
        for (int r = 0; r < g.length; r++) {
            for (int c = 0; c <= g[r].length - word.length(); c++) {
                if (matchesRight(g, r, c, word)) return true;
            }
        }
        return false;
    }

    static boolean matchesRight(char[][] g, int r, int c, String word) {
        for (int i = 0; i < word.length(); i++) {
            if (g[r][c + i] != word.charAt(i)) return false;
        }
        return true;
    }

    static boolean findVertical(char[][] g, String word) {
        int rows = g.length;
        int cols = g[0].length;

        for (int c = 0; c < cols; c++) {
            for (int r = 0; r <= rows - word.length(); r++) {
                if (matchesDown(g, r, c, word)) return true;
            }
        }
        return false;
    }

    static boolean matchesDown(char[][] g, int r, int c, String word) {
        for (int i = 0; i < word.length(); i++) {
            if (g[r + i][c] != word.charAt(i)) return false;
        }
        return true;
    }
}
