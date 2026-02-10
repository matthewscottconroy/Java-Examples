public class TrimmingAndCaseTransformation {
    public static void main(String[] args) {
        String original = "  Java Programming  ";

        String trimmed = original.trim();
        String upper = trimmed.toUpperCase();

        System.out.println(original);
        System.out.println(trimmed);
        System.out.println(upper);
    }
}

