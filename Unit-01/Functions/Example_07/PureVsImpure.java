public class PureVsImpure {
    static int squarePure(int x) {
        return x * x;
    }

    static void squareImpure(int x) {
        System.out.println(x * x);
    }

    public static void main(String[] args) {
        int a = squarePure(7);
        System.out.println("stored: " + a);

        System.out.print("printed: ");
        squareImpure(7);
    }
}

