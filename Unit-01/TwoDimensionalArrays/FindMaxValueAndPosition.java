public class FindMaxValueAndPosition {
    public static void main(String[] args) {
        int[][] grid = {
            {3, 1, 4},
            {1, 5, 9},
            {2, 6, 5}
        };

        int bestR = 0;
        int bestC = 0;
        int bestVal = grid[0][0];

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] > bestVal) {
                    bestVal = grid[r][c];
                    bestR = r;
                    bestC = c;
                }
            }
        }

        System.out.println("Max: " + bestVal + " at (" + bestR + ", " + bestC + ")");
    }
}

