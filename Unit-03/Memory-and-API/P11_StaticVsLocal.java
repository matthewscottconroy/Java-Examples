// File: P11_StaticVsLocal.java
public class P11_StaticVsLocal {

    static int counter = 0; // static: one per class, lives as long as class is loaded

    static void doWork() {
        Debug.sep("inside doWork()");
        int local = 0; // local: in stack frame, created each call
        counter++;
        local++;
        System.out.println("counter (static) = " + counter);
        System.out.println("local (stack)    = " + local);
    }

    public static void main(String[] args) {
        Debug.sep("static persists across calls; locals do not");
        doWork();
        doWork();
        doWork();
    }
}
