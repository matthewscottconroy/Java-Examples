import java.util.Arrays;

public class ScalarMultiply2D {
    public static void main(String[] args) {
        int[][] a = {
                {1, 2, 3},
                {4, 5, 6}
        };
        int k = 3;

        int[][] b = new int[a.length][a[0].length];

        for (int r = 0; r < a.length; r++) {
            for (int c = 0; c < a[r].length; c++) {
                b[r][c] = a[r][c] * k;
            }
        }

        System.out.println("A: " + Arrays.deepToString(a));
        System.out.println("k: " + k);
        System.out.println("B = k*A: " + Arrays.deepToString(b));
    }
}
