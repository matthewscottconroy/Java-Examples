public class P06_Throw {

    static int parseAge(String s) {
        int age = Integer.parseInt(s);

        if (age < 0) {
            // You decide this state is invalid.
            throw new IllegalArgumentException("Age cannot be negative: " + age);
        }

        return age;
    }

    public static void main(String[] args) {
        try {
            int age = parseAge("-5");
            System.out.println("Age = " + age);
        } catch (IllegalArgumentException e) {
            System.out.println("Bad input: " + e.getMessage());
        }
    }
}
