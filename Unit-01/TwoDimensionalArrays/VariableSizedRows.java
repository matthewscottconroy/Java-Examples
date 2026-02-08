public class VariableSizedRows {
    public static void main(String[] args) {
        int[][] ragged = {
            {1, 2, 3, 4},
            {9, 8},
            {7, 6, 5}
        };

        for (int r = 0; r < ragged.length; r++) {
            for (int c = 0; c < ragged[r].length; c++) {
                System.out.print(ragged[r][c]);
                if (c < ragged[r].length - 1) System.out.print(" ");
            }
            System.out.println();
        }
    }
}

