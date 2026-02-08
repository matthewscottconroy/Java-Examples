public class FilterStyleFunction {
    static int[] evensOnly(int[] a) {
        int count = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] % 2 == 0) count++;
        }

        int[] b = new int[count];
        int j = 0;

        for (int i = 0; i < a.length; i++) {
            if (a[i] % 2 == 0) {
                b[j] = a[i];
                j++;
            }
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
        int[] nums = {9, 2, 7, 4, 6, 5, 8};
        printArrayOneLine(evensOnly(nums));
    }
}

