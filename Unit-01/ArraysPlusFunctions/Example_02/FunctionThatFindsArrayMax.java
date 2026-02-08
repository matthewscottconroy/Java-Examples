public class FunctionThatFindsArrayMax {
    static int max(int[] a) {
        int m = a[0];
        for (int i = 1; i < a.length; i++) {
            if (a[i] > m) m = a[i];
        }
        return m;
    }

    public static void main(String[] args) {
        int[] nums = {42, 17, 88, 3, 29};
        System.out.println("Max: " + max(nums));
    }
}

