import java.util.*;

public class CollectionBasics {
    public static void main(String[] args) {
        Collection<String> names = new ArrayList<>();

        names.add("Ada");
        names.add("Grace");
        names.add("Ada"); // duplicates allowed in a List backing

        System.out.println("size: " + names.size());
        System.out.println("contains Ada? " + names.contains("Ada"));
        System.out.println(names);
    }
}

