public class ClassesWithConstructors {
    int count;

    ClassesWithConstructors(int initialCount) {
        count = initialCount;
    }

    public static void main(String[] args) {
        ClassesWithConstructors a = new ClassesWithConstructors(5);
        ClassesWithConstructors b = new ClassesWithConstructors(100);

        System.out.println(a.count);
        System.out.println(b.count);
    }
}

