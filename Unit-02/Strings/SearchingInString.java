public class SearchingInString{
    public static void main(String[] args) {
        String sentence = "The quick brown fox";

        System.out.println(sentence.contains("quick"));
        System.out.println(sentence.indexOf("fox"));
        System.out.println(sentence.indexOf("dog"));
    }
}

