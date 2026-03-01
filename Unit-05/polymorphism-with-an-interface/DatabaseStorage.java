import java.util.HashMap;
import java.util.Map;

public class DatabaseStorage implements Storage {
    private Map<Integer, String> database = new HashMap<>();
    private int idCounter = 0;

    @Override
    public void save(String data) {
        database.put(++idCounter, data);
        System.out.println("Saved record " + idCounter + " to the database.");
    }

    @Override
    public String load() {
        if (database.isEmpty()) return "No records found in database.";
        return "Latest DB entry: " + database.get(idCounter);
    }
}

