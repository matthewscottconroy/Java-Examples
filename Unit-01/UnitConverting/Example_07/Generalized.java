public class Generalized {

    public static void main(String[] args) {

        // 1) Validate arguments
        if (!hasCorrectArgCount(args)) {
            printUsage();
	    System.exit(1);
        }

        // 2) Normalize units
        String fromUnit = normalizeUnit(args[0]);
        String toUnit   = normalizeUnit(args[1]);

        // 3) Parse amount
        Double amount = parseAmount(args[2]);
        if (amount == null) {
	    System.exit(1);
        }

        // 4) Look up "meters per unit" factors
        Double metersPerFrom = metersPerUnit(fromUnit);
        Double metersPerTo   = metersPerUnit(toUnit);

        if (metersPerFrom == null) {
            System.err.println("Error: unknown fromUnit: " + args[0]);
            printUsage();
	    System.exit(1);
        }

        if (metersPerTo == null) {
            System.err.println("Error: unknown toUnit: " + args[1]);
            printUsage();
	    System.exit(1);
        }

        // 5) Convert fromUnit -> meters (multiply by metersPerFrom)
        Converter toMeters = new Converter(fromUnit, amount, metersPerFrom, true);
        double meters = toMeters.convert();

        // 6) Convert meters -> toUnit (divide by metersPerTo)
        Converter fromMeters = new Converter(toUnit, meters, metersPerTo, false);
        double result = fromMeters.convert();

        // 7) Print result
        System.out.println(amount + " " + fromUnit + " = " + result + " " + toUnit);
    }

    // ---------------- Subroutines ----------------

    static boolean hasCorrectArgCount(String[] args) {
        return args.length == 3;
    }

    static void printUsage() {
        System.err.println("Usage: java Generalized <fromUnit> <toUnit> <amount>");
        System.err.println("Supported metric units: mm, cm, m, km");
        System.err.println("Supported English units: in, ft, yd, mi");
        System.err.println("Examples:");
        System.err.println("  java Generalized mi km 3");
        System.err.println("  java Generalized ft in 6");
        System.err.println("  java Generalized cm m 250");
        System.err.println("  java Generalized yd m 10");
    }

    static String normalizeUnit(String unit) {
        String u = unit.trim().toLowerCase();

        // Metric synonyms
        if (u.equals("millimeter") || u.equals("millimeters")) return "mm";
        if (u.equals("centimeter") || u.equals("centimeters")) return "cm";
        if (u.equals("meter") || u.equals("meters")) return "m";
        if (u.equals("kilometer") || u.equals("kilometers")) return "km";

        // English synonyms
        if (u.equals("inch") || u.equals("inches")) return "in";
        if (u.equals("foot") || u.equals("feet")) return "ft";
        if (u.equals("yard") || u.equals("yards")) return "yd";
        if (u.equals("mile") || u.equals("miles")) return "mi";

        // If already an abbreviation (or unknown), return it as-is.
        return u;
    }

    static Double parseAmount(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            System.err.println("Error: <amount> must be a number. You gave: " + text);
            return null;
        }
    }

    static Double metersPerUnit(String unit) {
        // Metric
        if (unit.equals("mm")) return 0.001;
        if (unit.equals("cm")) return 0.01;
        if (unit.equals("m"))  return 1.0;
        if (unit.equals("km")) return 1000.0;

        // English
        // (exact definitions)
        if (unit.equals("in")) return 0.0254;
        if (unit.equals("ft")) return 0.3048;
        if (unit.equals("yd")) return 0.9144;
        if (unit.equals("mi")) return 1609.344;

        return null;
    }
}

