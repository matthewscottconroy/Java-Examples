public class FunctionThatReturnsArraySum {
    static int sum(int[] a) {
        int total = 0;
        for (int i = 0; i < a.length; i++) {
            total += a[i];
        }
        return total;
    }

    public static void main(String[] args) {
        int[] nums = {3, 5, 7, 9};
        System.out.println("Sum: " + sum(nums));
    }
}

