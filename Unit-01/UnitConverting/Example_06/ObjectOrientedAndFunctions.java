public class ObjectOrientedAndFunctions {

    public static void main(String[] args) {

        // 1) Validate argument count
        if (!hasCorrectArgCount(args)) {
            printUsage();
	    System.exit(1);
        }

        // 2) Normalize unit
        String unit = normalizeUnit(args[0]);

        // 3) Parse amount
        Double amount = parseAmount(args[1]);
        if (amount == null) {
	    System.exit(1);
        }

        // 4) Build the appropriate Converter
        Converter converter = buildConverter(unit, amount);
        if (converter == null) {
            System.err.println("Error: unknown unit: " + args[0]);
            printUsage();
	    System.exit(1);
        }

        // 5) Convert and print
        double result = converter.convert();
        System.out.println(formatOutput(converter, result));
    }

    // ---------------- Subroutines ----------------

    static boolean hasCorrectArgCount(String[] args) {
        return args.length == 2;
    }

    static void printUsage() {
        System.err.println("Usage: java ObjectOrientedAndFunctions <starting unit> <amount>");
        System.err.println("Supported units: in, cm, mi, km");
        System.err.println("Examples:");
        System.err.println("  java ObjectOrientedAndFunctions in 12");
        System.err.println("  java ObjectOrientedAndFunctions cm 30");
        System.err.println("  java ObjectOrientedAndFunctions mi 3");
        System.err.println("  java ObjectOrientedAndFunctions km 5");
    }

    static String normalizeUnit(String unit) {
        return unit.trim().toLowerCase();
    }

    static Double parseAmount(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            System.err.println("Error: <amount> must be a number. You gave: " + text);
            return null;
        }
    }

    static Converter buildConverter(String unit, double amount) {
        // Constants
        double inchesToCentimeters = 2.54;
        double milesToKilometers = 1.60934;

        // Choose converter based on starting unit
        if (unit.equals("in") || unit.equals("inch") || unit.equals("inches")) {
            // in -> cm (multiply)
            return new Converter("in", "cm", amount, inchesToCentimeters, true);

        } else if (unit.equals("cm") || unit.equals("centimeter") || unit.equals("centimeters")) {
            // cm -> in (divide)
            return new Converter("cm", "in", amount, inchesToCentimeters, false);

        } else if (unit.equals("mi") || unit.equals("mile") || unit.equals("miles")) {
            // mi -> km (multiply)
            return new Converter("mi", "km", amount, milesToKilometers, true);

        } else if (unit.equals("km") || unit.equals("kilometer") || unit.equals("kilometers")) {
            // km -> mi (divide)
            return new Converter("km", "mi", amount, milesToKilometers, false);
        }

        return null;
    }

    static String formatOutput(Converter converter, double result) {
        return converter.getAmount() + " " + converter.getFromUnit()
                + " = " + result + " " + converter.getToUnit();
    }
}
