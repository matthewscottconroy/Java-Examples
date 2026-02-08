public class FunctionThatReturnsArrayAverage {
    static double average(int[] a) {
        int total = 0;
        for (int i = 0; i < a.length; i++) {
            total += a[i];
        }
        return (double) total / a.length;
    }

    public static void main(String[] args) {
        int[] nums = {10, 20, 30, 40};
        System.out.println("Average: " + average(nums));
    }
}

