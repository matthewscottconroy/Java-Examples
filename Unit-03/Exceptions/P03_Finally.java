public class P03_Finally {
    public static void main(String[] args) {
        System.out.println("Start.");

        try {
            System.out.println("Inside try.");
            int[] a = {1, 2, 3};
            System.out.println(a[99]); // throws
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Caught: " + e.getClass().getSimpleName());
        } finally {
            System.out.println("Finally runs no matter what.");
        }

        System.out.println("End.");
    }
}
