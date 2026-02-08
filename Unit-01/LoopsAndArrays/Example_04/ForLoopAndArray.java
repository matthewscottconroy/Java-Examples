public class ForLoopWithArray {
    public static void main(String[] args) {
        int[] numbers = {10, 20, 30, 40, 50};

        for (int i = 0; i < numbers.length; i = i + 1) {
            System.out.println(numbers[i]);
        }
    }
}

