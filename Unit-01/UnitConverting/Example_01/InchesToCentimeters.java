public class InchesToCentimeters {
    public static void main(String[] args) {

        // 1) Check that we got exactly 1 command line arguments.
        if (args.length != 1) {
            System.err.println("Usage: java InchesToCentimeters <amount>");
            System.err.println("Example: java InchesToCentimeters 12");
	    System.exit(1);
        }

        // 3) Read the amount (1st argument) and try to parse it as a number.
        double amount = 0.0;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Error: <amount> must be a number. You gave: " + args[0]);
	    System.exit(1);
        }

        double result;
        result = amount * 2.54;

        System.out.println(amount + " inches = " + result + " centimeters");
    }
}

