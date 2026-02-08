public class TwoDimensionalPlusLoop {
    public static void main(String[] args) {
        int[][] grid = {
            {1, 2, 3},
            {4, 5, 6}
        };

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                System.out.println("grid[" + r + "][" + c + "] = " + grid[r][c]);
            }
        }
    }
}

