public class ClassB {
    private String value;

    public ClassB(String start) {
        value = start;
    }

    public void concat() {
        value = value + value;
    }

    public String getValue() {
        return value;
    }
}
