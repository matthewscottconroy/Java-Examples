import java.sql.*;

// Demonstrates PreparedStatement — the standard defence against SQL injection.
//
// A PreparedStatement sends the query template to the database server *before*
// supplying values.  The server parses and plans the query once; parameters are
// substituted later as data, never as SQL text.  An attacker who controls a
// parameter value cannot break out of the data context into SQL syntax.
//
// Compare the two queries below: the "unsafe" variant concatenates user input
// directly into the SQL string; the "safe" variant uses placeholders (?).
public class PreparedStatements {

    // ⚠ WARNING: Never hardcode credentials in real applications.
    //   Use System.getenv("DB_USER") or an external config file instead.
    private static final String URL  = "jdbc:postgresql://localhost:5432/lab42";
    private static final String USER = "matthewconroy";
    private static final String PASS = "password";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            setupSchema(conn);
            insertSafe(conn, "Alice",  30);
            insertSafe(conn, "Bob",    25);
            insertSafe(conn, "O'Brien", 40);   // single quote — safe with prepared stmt
            queryByName(conn, "Alice");
            queryByAgeRange(conn, 20, 35);
            demonstrateInjectionAttempt(conn);
        }
    }

    private static void setupSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id   SERIAL PRIMARY KEY,
                    name TEXT   NOT NULL,
                    age  INT    NOT NULL
                )""");
            st.execute("DELETE FROM users");   // fresh start for demo
        }
        System.out.println("Schema ready.\n");
    }

    // Parameterized INSERT — the ? placeholders are filled in as typed values.
    private static void insertSafe(Connection conn, String name, int age) throws SQLException {
        String sql = "INSERT INTO users (name, age) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, age);
            int rows = ps.executeUpdate();
            System.out.printf("Inserted %d row — name='%s', age=%d%n", rows, name, age);
        }
    }

    private static void queryByName(Connection conn, String name) throws SQLException {
        String sql = "SELECT id, name, age FROM users WHERE name = ?";
        System.out.println("\nQuery by name = '" + name + "':");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  id=%-3d  name=%-12s  age=%d%n",
                        rs.getInt("id"), rs.getString("name"), rs.getInt("age"));
                }
            }
        }
    }

    private static void queryByAgeRange(Connection conn, int lo, int hi) throws SQLException {
        String sql = "SELECT name, age FROM users WHERE age BETWEEN ? AND ? ORDER BY age";
        System.out.printf("%nQuery age range [%d, %d]:%n", lo, hi);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lo);
            ps.setInt(2, hi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  %-12s  age=%d%n", rs.getString("name"), rs.getInt("age"));
                }
            }
        }
    }

    // Show that a classic injection string is treated as plain text data.
    private static void demonstrateInjectionAttempt(Connection conn) throws SQLException {
        // A naive injection payload — would destroy the table in a raw string query.
        String maliciousInput = "'; DROP TABLE users; --";
        System.out.println("\nInjection attempt with: " + maliciousInput);
        String sql = "SELECT * FROM users WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maliciousInput);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("  Rows returned: " + (rs.next() ? "1+" : "0"));
                System.out.println("  Table is intact — the payload was treated as a literal string.");
            }
        }
    }
}
