public class ReverseInPlace {
    static void swap(int[] a, int i, int j) {
        int tmp = a[i];
        a[i] = a[j];
        a[j] = tmp;
    }

    static void reverseInPlace(int[] a) {
        int left = 0;
        int right = a.length - 1;

        while (left < right) {
            swap(a, left, right);
            left++;
            right--;
        }
    }

    static void printArrayOneLine(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]);
            if (i < a.length - 1) System.out.print(", ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3, 4, 5};
        printArrayOneLine(nums);
        reverseInPlace(nums);
        printArrayOneLine(nums);
    }
}

