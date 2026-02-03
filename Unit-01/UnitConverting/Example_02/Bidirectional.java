public class Bidirectional {
    public static void main(String[] args) {

        // 1) Check that we got exactly 2 command line arguments.
        if (args.length != 2) {
            System.err.println("Usage: java Bidirectional <starting unit> <amount>");
            System.err.println("Example: java Bidirectional in 12");
            System.err.println("Example: java Bidirectional cm 30");
	    System.exit(1);
        }

        // 2) Read the starting unit (first argument).
        String unit = args[0];

        // 3) Read the amount (second argument) and try to parse it as a number.
        double amount = 0.0;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: <amount> must be a number. You gave: " + args[1]);
	    System.exit(1);
        }

        // 4) Normalize the unit so IN, in, Inches, etc. are easier to handle.
        unit = unit.trim().toLowerCase();

        // 5) Decide which conversion to do.
        //    1 inch = 2.54 cm
        double result;

        if (unit.equals("in") || unit.equals("inch") || unit.equals("inches")) {
            result = amount * 2.54;
            System.out.println(amount + " inches = " + result + " centimeters");
        } else if (unit.equals("cm") || unit.equals("centimeter") || unit.equals("centimeters")) {
            result = amount / 2.54;
            System.out.println(amount + " centimeters = " + result + " inches");
        } else {
            System.err.println("Error: unknown unit: " + args[0]);
            System.err.println("Use: in / inch / inches  OR  cm / centimeter / centimeters");
	    System.exit(1);
        }
    }
}

