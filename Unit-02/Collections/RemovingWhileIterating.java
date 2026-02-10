import java.util.*;

public class RemovingWhileIterating {
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));

        Iterator<Integer> it = nums.iterator();
        while (it.hasNext()) {
            int n = it.next();
            if (n % 2 == 0) {
                it.remove(); // safe removal during iteration
            }
        }

        System.out.println(nums); // [1, 3, 5]
    }
}

