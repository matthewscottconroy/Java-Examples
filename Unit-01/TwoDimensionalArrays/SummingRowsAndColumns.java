public class SummingRowsAndColumns {
    static int rowSum(int[][] grid, int row) {
        int sum = 0;
        for (int c = 0; c < grid[row].length; c++) {
            sum += grid[row][c];
        }
        return sum;
    }

    static int colSum(int[][] grid, int col) {
        int sum = 0;
        for (int r = 0; r < grid.length; r++) {
            // assume rectangular for this example
            sum += grid[r][col];
        }
        return sum;
    }

    public static void main(String[] args) {
        int[][] grid = {
            {3, 1, 4},
            {1, 5, 9},
            {2, 6, 5}
        };

        System.out.println("Row 0 sum: " + rowSum(grid, 0));
        System.out.println("Row 1 sum: " + rowSum(grid, 1));
        System.out.println("Col 0 sum: " + colSum(grid, 0));
        System.out.println("Col 2 sum: " + colSum(grid, 2));
    }
}

