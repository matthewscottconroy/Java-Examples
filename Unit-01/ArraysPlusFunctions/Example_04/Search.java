public class Search {
    static int indexOf(int[] a, int target) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == target) return i;
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] nums = {5, 10, 15, 20};
        System.out.println(indexOf(nums, 15)); // 2
        System.out.println(indexOf(nums, 99)); // -1
    }
}

