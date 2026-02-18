import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class P12_CsvModifySimple {
    public static void main(String[] args) throws IOException {
        Path input = Paths.get(args.length > 0 ? args[0] : "people.csv");
        Path output = Paths.get(args.length > 1 ? args[1] : "people_out.csv");

        List<String[]> rows = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(input, StandardCharsets.UTF_8)) {
            String header = br.readLine();
            if (header == null) throw new IOException("Empty CSV");

            rows.add(splitCsvSimple(header));

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                rows.add(splitCsvSimple(line));
            }
        }

        // Modify: bump score if < 50 (assumes columns: id,name,score)
        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            int score = Integer.parseInt(r[2].trim());
            if (score < 50) score += 10;
            r[2] = Integer.toString(score);
        }

        // Write out
        try (BufferedWriter bw = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (String[] r : rows) {
                bw.write(String.join(",", r));
                bw.newLine();
            }
        }

        System.out.println("Wrote modified CSV: " + output.toAbsolutePath());
        System.out.println("NOTE: This parser is 'simple CSV' only (no quoted commas).");
    }

    static String[] splitCsvSimple(String line) {
        // Good enough for teaching basic file processing; not a full CSV parser.
        return line.split(",", -1);
    }
}
