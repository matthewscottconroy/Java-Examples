public class MapStyleFunction {
    static int[] scaleCopy(int[] a, int factor) {
        int[] b = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            b[i] = a[i] * factor;
        }
        return b;
    }

    static void printArrayOneLine(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i < a.length - 1) System.out.print(", ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] nums = {2, 4, 6};
        printArrayOneLine(scaleCopy(nums, 3));
        printArrayOneLine(scaleCopy(nums, -1));
    }
}

