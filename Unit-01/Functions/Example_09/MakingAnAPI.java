class AnAPI {
    static int clamp(int x, int lo, int hi) {
        if (lo > hi) {
            throw new IllegalArgumentException("lo must be <= hi");
        }
        if (x < lo) return lo;
        if (x > hi) return hi;
        return x;
    }

    static int abs(int x) {
        return x < 0 ? -x : x;
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println(AnAPI.clamp(15, 0, 10));   // 10
        System.out.println(AnAPI.clamp(-3, 0, 10));   // 0
        System.out.println(AnAPI.abs(-42));           // 42
    }
}

