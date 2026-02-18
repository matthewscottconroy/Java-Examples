// File: P12_ConstructorsMemory.java
public class P12_ConstructorsMemory {

    static class Widget {
        static int nextId = 1;

        final int id;      // assigned in constructor
        String name;       // assigned in constructor or initializer
        int[] data;        // array allocated in constructor

        // instance initializer runs BEFORE constructor body
        {
            name = "(default name from initializer)";
            System.out.println("instance initializer: this -> " + Debug.id(this) + ", name=" + name);
        }

        Widget() {
            this.id = nextId++;
            this.data = new int[3];
            System.out.println("constructor Widget(): this -> " + Debug.id(this) +
                    ", id=" + id + ", name=" + name + ", data -> " + Debug.id(data));
        }

        Widget(String name) {
            this.id = nextId++;
            this.name = name;
            this.data = new int[5];
            System.out.println("constructor Widget(String): this -> " + Debug.id(this) +
                    ", id=" + id + ", name=" + this.name + ", data -> " + Debug.id(data));
        }
    }

    public static void main(String[] args) {
        Debug.sep("constructors allocate object then initialize fields");
        Widget w1 = new Widget();
        System.out.println("w1 -> " + Debug.id(w1) + " data -> " + Debug.id(w1.data));

        Debug.sep("overloaded constructor");
        Widget w2 = new Widget("Custom");
        System.out.println("w2 -> " + Debug.id(w2) + " data -> " + Debug.id(w2.data));

        Debug.sep("static field persists");
        System.out.println("Widget.nextId = " + Widget.nextId);
    }
}
