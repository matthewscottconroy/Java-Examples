import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class PostgresConnectionExample {

    public static void main(String[] args) {
        // Database connection parameters.
        // ⚠ WARNING: Never hardcode credentials in real applications.
        //   Use environment variables (System.getenv("DB_USER")) or an external
        //   configuration file that is excluded from version control instead.
        String url = "jdbc:postgresql://localhost:5432/lab42";
        String user = "matthewconroy";
        String password = "password";

        // Attempt to connect to the PostgreSQL database
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to the PostgreSQL server successfully.");

            // Create a statement to execute a query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT version()")) {

                if (rs.next()) {
                    System.out.println("PostgreSQL version: " + rs.getString(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Connection failure: " + e.getMessage());
        }
    }
}

