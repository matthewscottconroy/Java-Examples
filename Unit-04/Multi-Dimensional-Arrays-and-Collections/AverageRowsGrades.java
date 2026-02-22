import java.util.Arrays;

public class AverageRowsGrades {
    public static void main(String[] args) {
        int[][] grades = {
                {90, 85, 88},
                {72, 81, 79},
                {100, 95, 98},
                {60, 70, 65}
        };

        double[] averages = new double[grades.length];

        for (int r = 0; r < grades.length; r++) {
            int sum = 0;
            for (int c = 0; c < grades[r].length; c++) {
                sum += grades[r][c];
            }
            averages[r] = (double) sum / grades[r].length;
        }

        System.out.println("Grades: " + Arrays.deepToString(grades));
        System.out.println("Row averages: " + Arrays.toString(averages));
    }
}
