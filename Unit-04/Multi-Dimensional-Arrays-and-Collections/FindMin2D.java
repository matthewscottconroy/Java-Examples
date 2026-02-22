import java.util.Arrays;

public class FindMin2D {
    public static void main(String[] args) {
        int[][] data = {
                {9,  2,  7},
                {3,  11, 5},
                {8,  1,  6}
        };

        int min = data[0][0];
        int minR = 0, minC = 0;

        for (int r = 0; r < data.length; r++) {
            for (int c = 0; c < data[r].length; c++) {
                if (data[r][c] < min) {
                    min = data[r][c];
                    minR = r;
                    minC = c;
                }
            }
        }

        System.out.println("Data: " + Arrays.deepToString(data));
        System.out.printf("Min = %d at (%d,%d)%n", min, minR, minC);
    }
}
