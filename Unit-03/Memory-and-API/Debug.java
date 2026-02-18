// File: Debug.java
public class Debug {
    public static String id(Object o) {
        if (o == null) return "null";
        return o.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(o));
    }

    public static void sep(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
