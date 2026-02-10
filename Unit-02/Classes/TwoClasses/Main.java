public class Main {
	public static void main(String[] args){
   		ClassA a = new ClassA(2);
		ClassB b = new ClassB("atcg");

		print(a, b);

		System.out.println("");
		System.out.println("Mutating the objects...");
		a.multiplyByTwo();
		b.concat();
		System.out.println("");

		print(a, b);
	}

	private static void print(ClassA a, ClassB b){
		System.out.println("a: " + a.getValue());
		System.out.println("b: " + b.getValue());
	}
}
