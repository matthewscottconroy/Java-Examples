public class ComparingStrings {
    public static void main(String[] args) {
        String a = "hello";
	String b = "hello";
        String c = new String("hello");


	System.out.println("a: " + a);
	System.out.println("b: " + b);
	System.out.println("c: " + c);

	System.out.println("Comparing a and b: ");
        System.out.println(a == b);
        System.out.println(a.equals(b));

	System.out.println("Comparing a and c: ");
	System.out.println(a == c);
	System.out.println(a.equals(c));
    }
}
