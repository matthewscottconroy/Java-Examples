// File: P02_Objects.java
public class P02_Objects {
    static class Person {
        String name;
        int age;
        Person(String name, int age) { this.name = name; this.age = age; }
        public String toString() { return "Person{name=" + name + ", age=" + age + "}"; }
    }

    public static void main(String[] args) {
        Debug.sep("object variables store a REFERENCE (in stack), object lives on HEAP");

        Person p = new Person("Ada", 36);

        System.out.println("p (variable) points to: " + Debug.id(p));
        System.out.println("p contents: " + p);

        // The reference is stored in main's stack frame.
        // The Person object + its fields live on the heap.
        // The String name is ALSO a reference to a String object on the heap.
        System.out.println("p.name points to: " + Debug.id(p.name));
    }
}
