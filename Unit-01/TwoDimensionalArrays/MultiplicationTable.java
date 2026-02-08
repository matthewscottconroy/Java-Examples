public class MultiplicationTable {
    static int[][] makeTimesTable(int rows, int cols) {
        int[][] table = new int[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                table[r][c] = (r + 1) * (c + 1);
            }
        }

        return table;
    }

    static void printGrid(int[][] grid) {
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                System.out.print(grid[r][c]);
                if (c < grid[r].length - 1) System.out.print("\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int[][] table = makeTimesTable(5, 5);
        printGrid(table);
    }
}

