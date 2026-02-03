public class InchesOrMiles {
    public static void main(String[] args) {

        // 1) Check argument count
        if (args.length != 2) {
            System.err.println("Usage: java InchesOrMiles <starting unit> <amount>");
            System.err.println("Units: in, cm, mi, km");
	    System.exit(1);
        }

        // 2) Read unit
        String unit = args[0].trim().toLowerCase();

        // 3) Parse amount
        double amount = 0.0;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: amount must be a number");
	    System.exit(1);
        }

        // 4) Conversion constants
        double inchesToCentimeters = 2.54;
        double milesToKilometers = 1.60934;

        // 5) Perform conversion
        double result;

        if (unit.equals("in") || unit.equals("inch") || unit.equals("inches")) {

            result = amount * inchesToCentimeters;
            System.out.println(amount + " inches = " + result + " centimeters");

        } else if (unit.equals("cm") || unit.equals("centimeter") || unit.equals("centimeters")) {

            result = amount / inchesToCentimeters;
            System.out.println(amount + " centimeters = " + result + " inches");

        } else if (unit.equals("mi") || unit.equals("mile") || unit.equals("miles")) {

            result = amount * milesToKilometers;
            System.out.println(amount + " miles = " + result + " kilometers");

        } else if (unit.equals("km") || unit.equals("kilometer") || unit.equals("kilometers")) {

            result = amount / milesToKilometers;
            System.out.println(amount + " kilometers = " + result + " miles");

        } else {
            System.err.println("Error: unknown unit: " + args[0]);
            System.err.println("Supported units: in, cm, mi, km");
	    System.exit(1);
        }
    }
}

