public class ClassesHaveBehavior{
    int count;

    void increment() {
        count = count + 1;
    }

    public static void main(String[] args) {
        ClassesHaveBehavior e = new ClassesHaveBehavior();
        e.count = 0;

        e.increment();
        e.increment();

        System.out.println(e.count);
    }
}

