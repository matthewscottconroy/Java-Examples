import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStorage implements Storage {
    private final Path filePath;

    public FileStorage(String filename) {
        this.filePath = Path.of(filename);
    }

    @Override
    public void save(String data) {
        try (FileWriter writer = new FileWriter(filePath.toFile(), true)) {
            writer.write(data + "\n");
            System.out.println("Saved data to file: " + filePath);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    @Override
    public String load() {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }
}

