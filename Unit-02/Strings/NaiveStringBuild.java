public class NaiveStringBuild {
    public static void main(String[] args) {
        String result = "";

        for (int i = 0; i < 5; i++) {
            result = result + i;
        }

        System.out.println(result);
    }
}

