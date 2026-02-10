import java.util.*;

public class UsingMaps {
    public static void main(String[] args) {
        Map<String, Integer> counts = new HashMap<>();

        String[] words = {"java", "java", "collections", "map", "java", "set"};

        for (String w : words) {
            counts.put(w, counts.getOrDefault(w, 0) + 1);
        }

        System.out.println(counts);

        // Iterate keys
        for (String key : counts.keySet()) {
            System.out.println("key: " + key);
        }

        // Iterate values
        for (int v : counts.values()) {
            System.out.println("value: " + v);
        }

        // Iterate entries (most common)
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
    }
}

