public class Counting {
    public static void main(String[] args) {
        int[] numbers = {2, 7, 4, 9, 6, 1};

        int countEven = 0;

        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] % 2 == 0) {
                countEven++;
            }
        }

        System.out.println("Even numbers: " + countEven);
    }
}

