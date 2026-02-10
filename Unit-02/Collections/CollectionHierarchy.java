import java.util.*;

public class CollectionHierarchy {
    public static void main(String[] args) {
        ArrayList<String> a = new ArrayList<>();
        Collection<String> c = a;   // List -> Collection
        Iterable<String> i = a;     // Collection -> Iterable

        System.out.println("a is a List: " + (a instanceof List));
        System.out.println("a is a Collection: " + (a instanceof Collection));
        System.out.println("a is an Iterable: " + (a instanceof Iterable));

        HashMap<String, Integer> m = new HashMap<>();
        System.out.println("m is a Map: " + (m instanceof Map));
        // NOTE: Map is NOT a Collection
        System.out.println("m is a Collection: " + (m instanceof Collection));
    }
}

