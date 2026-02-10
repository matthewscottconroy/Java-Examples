public class ExtractingSubstrings {
    public static void main(String[] args) {
        String filename = "report_2026_final.pdf";

        String year = filename.substring(7, 11);
        String extension = filename.substring(filename.length() - 3);

        System.out.println(year);
        System.out.println(extension);
    }
}

