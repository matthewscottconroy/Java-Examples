/********************************************
 *  This program performs the conversions
 *  just like before but is divided into
 *  subroutines.
 ********************************************/
public class Functions {

    public static void main(String[] args) {

        // 1) Check arguments
        if (!validArgumentCount(args)) {
            printUsage();
	    System.exit(1);
        }

        // 2) Parse unit
        String unit = normalizeUnit(args[0]);

        // 3) Parse amount
        double amount = parseAmount(args[1]);
        if (amount < 0) {
	    System.exit(1);
        }

        // 4) Perform conversion
        convertAndPrint(unit, amount);
    }

    // ---------------- Subroutines ----------------

    static boolean validArgumentCount(String[] args) {
        return args.length == 2;
    }

    static void printUsage() {
        System.err.println("Usage: java Functions <starting unit> <amount>");
        System.err.println("Units: in, cm, mi, km");
    }

    static String normalizeUnit(String unit) {
        return unit.trim().toLowerCase();
    }

    static double parseAmount(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.err.println("Error: amount must be a number");
            return -1;
        }
    }

    static void convertAndPrint(String unit, double amount) {

        double inchesToCentimeters = 2.54;
        double milesToKilometers = 1.60934;
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
            System.err.println("Error: unknown unit: " + unit);
            System.err.println("Supported units: in, cm, mi, km");
        }
    }
}

