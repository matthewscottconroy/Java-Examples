/*******************************
 * This program is an iteration
 * of the unit conversion 
 * program that uses objects
 * to represent the conversion.
 ******************************/
public class ObjectOriented {

    public static void main(String[] args) {

        // 1) Check arguments
        if (args.length != 2) {
            System.err.println("Usage: java ObjectOriented <unit> <amount>");
            System.err.println("Units: in, cm, mi, km");
	    System.exit(1);
        }

        // 2) Normalize unit
        String unit = args[0].trim().toLowerCase();

        // 3) Parse amount
        double amount = 0.0;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: amount must be numeric");
	    System.exit(1);
        }

        // 4) Create converter based on unit
        Converter converter;

        if (unit.equals("in")) {
            converter = new Converter("inches", amount, 2.54, true);
            System.out.println(amount + " inches = " + converter.convert() + " centimeters");

        } else if (unit.equals("cm")) {
            converter = new Converter("centimeters", amount, 2.54, false);
            System.out.println(amount + " centimeters = " + converter.convert() + " inches");

        } else if (unit.equals("mi")) {
            converter = new Converter("miles", amount, 1.60934, true);
            System.out.println(amount + " miles = " + converter.convert() + " kilometers");

        } else if (unit.equals("km")) {
            converter = new Converter("kilometers", amount, 1.60934, false);
            System.out.println(amount + " kilometers = " + converter.convert() + " miles");

        } else {
            System.err.println("Unknown unit: " + unit);
	    System.exit(1);
        }
    }
}

