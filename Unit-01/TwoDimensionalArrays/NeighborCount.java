public class NeighborCount {
    static int countNeighbors4(int[][] grid, int r, int c) {
        int count = 0;
        if (grid[r - 1][c] == 1) count++;
        if (grid[r + 1][c] == 1) count++;
        if (grid[r][c - 1] == 1) count++;
        if (grid[r][c + 1] == 1) count++;
        return count;
    }

    public static void main(String[] args) {
        int[][] grid = {
            {0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0},
            {0, 0, 1, 0, 0},
            {0, 0, 0, 0, 0}
        };

        int r = 2, c = 2; // a cell not on the edge
        System.out.println("4-neighbors of (" + r + "," + c + "): " + countNeighbors4(grid, r, c));
    }
}

