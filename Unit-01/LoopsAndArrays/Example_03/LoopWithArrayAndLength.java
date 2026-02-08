public class LoopWithArrayAndLength {
    public static void main(String[] args) {
        int[] numbers = {10, 20, 30, 40, 50};

        int i = 0;
        while (i < numbers.length) {
            System.out.println(numbers[i]);
            i = i + 1;
        }
    }
}

