import java.util.Arrays;

public class MaxPerColumn {
    public static void main(String[] args) {
        int[][] data = {
                {5,  12, 9},
                {10, 3,  14},
                {7,  20, 1}
        };

        int rows = data.length;
        int cols = data[0].length;

        int[] colMax = new int[cols];
        for (int c = 0; c < cols; c++) {
            colMax[c] = data[0][c];
        }

        for (int r = 1; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                colMax[c] = Math.max(colMax[c], data[r][c]);
            }
        }

        System.out.println("Data: " + Arrays.deepToString(data));
        System.out.println("Max per column: " + Arrays.toString(colMax));
    }
}
