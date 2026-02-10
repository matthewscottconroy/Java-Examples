import java.util.*;

public class WorkingWithSets {
    public static void main(String[] args) {
        List<String> raw = List.of("kiwi", "apple", "kiwi", "banana", "apple");

        Set<String> hash = new HashSet<>(raw);
        Set<String> linked = new LinkedHashSet<>(raw);
        Set<String> tree = new TreeSet<>(raw);

        System.out.println("HashSet (no order guarantee): " + hash);
        System.out.println("LinkedHashSet (insertion order): " + linked);
        System.out.println("TreeSet (sorted): " + tree);
    }
}

