import java.util.Arrays;

public class SumColumns {
    public static void main(String[] args) {
        int[][] data = {
                {3,  5,  2},
                {10, 1,  7},
                {4,  8,  6}
        };

        int rows = data.length;
        int cols = data[0].length;
        int[] colSums = new int[cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                colSums[c] += data[r][c];
            }
        }

        System.out.println("Data: " + Arrays.deepToString(data));
        System.out.println("Column sums: " + Arrays.toString(colSums));
    }
}
