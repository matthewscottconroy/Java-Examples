public class ArrayAPI {
    static void print(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i < a.length - 1) System.out.print(", ");
        }
        System.out.println();
    }

    static int sum(int[] a) {
        int total = 0;
        for (int i = 0; i < a.length; i++) total += a[i];
        return total;
    }

    static int max(int[] a) {
        int m = a[0];
        for (int i = 1; i < a.length; i++) if (a[i] > m) m = a[i];
        return m;
    }

    static int[] scaleCopy(int[] a, int factor) {
        int[] b = new int[a.length];
        for (int i = 0; i < a.length; i++) b[i] = a[i] * factor;
        return b;
    }

    public static void main(String[] args) {
        int[] nums = {3, 1, 4, 1, 5, 9};

        print(nums);
        System.out.println("Sum: " + sum(nums));
        System.out.println("Max: " + max(nums));
        print(scaleCopy(nums, 10));
    }
}

