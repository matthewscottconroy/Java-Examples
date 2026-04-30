import java.sql.*;

// Full CRUD (Create, Read, Update, Delete) lifecycle against a single table.
//
// Key concepts:
//   executeUpdate() — returns the number of rows affected (INSERT/UPDATE/DELETE).
//   Statement.RETURN_GENERATED_KEYS — retrieves auto-generated primary keys.
//   executeQuery()  — returns a ResultSet (SELECT).
//   Iterating a ResultSet with rs.next() — moves the cursor row by row.
public class CrudOperations {

    // ⚠ WARNING: Never hardcode credentials in real applications.
    private static final String URL  = "jdbc:postgresql://localhost:5432/lab42";
    private static final String USER = "matthewconroy";
    private static final String PASS = "password";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            setupSchema(conn);

            // CREATE
            long id1 = insert(conn, "Widget",  9.99,  100);
            long id2 = insert(conn, "Gadget",  24.50, 50);
            long id3 = insert(conn, "Doohickey", 4.75, 200);
            System.out.println();

            // READ — all rows
            System.out.println("All products after insert:");
            printAll(conn);

            // UPDATE — price and stock
            update(conn, id1, 12.99, 90);
            System.out.println("\nAfter updating id=" + id1 + ":");
            printAll(conn);

            // DELETE — one row
            delete(conn, id3);
            System.out.println("\nAfter deleting id=" + id3 + ":");
            printAll(conn);

            // READ — filtered
            System.out.println("\nProducts with stock > 60:");
            queryByStock(conn, 60);
        }
    }

    private static void setupSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id    SERIAL PRIMARY KEY,
                    name  TEXT           NOT NULL,
                    price NUMERIC(10, 2) NOT NULL,
                    stock INT            NOT NULL
                )""");
            st.execute("DELETE FROM products");
        }
    }

    // INSERT — returns the generated primary key.
    private static long insert(Connection conn, String name, double price, int stock)
            throws SQLException {
        String sql = "INSERT INTO products (name, price, stock) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, stock);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                long id = keys.getLong(1);
                System.out.printf("INSERT  id=%-3d  name=%-12s  price=%6.2f  stock=%d%n",
                    id, name, price, stock);
                return id;
            }
        }
    }

    // UPDATE — returns number of rows changed.
    private static int update(Connection conn, long id, double newPrice, int newStock)
            throws SQLException {
        String sql = "UPDATE products SET price = ?, stock = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, newStock);
            ps.setLong(3, id);
            int rows = ps.executeUpdate();
            System.out.printf("UPDATE  id=%-3d  new price=%.2f  new stock=%d  (%d row affected)%n",
                id, newPrice, newStock, rows);
            return rows;
        }
    }

    // DELETE — returns number of rows removed.
    private static int delete(Connection conn, long id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            System.out.printf("DELETE  id=%-3d  (%d row removed)%n", id, rows);
            return rows;
        }
    }

    private static void printAll(Connection conn) throws SQLException {
        String sql = "SELECT id, name, price, stock FROM products ORDER BY id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("  id=%-3d  name=%-12s  price=%6.2f  stock=%d%n",
                    rs.getLong("id"), rs.getString("name"),
                    rs.getDouble("price"), rs.getInt("stock"));
            }
        }
    }

    private static void queryByStock(Connection conn, int minStock) throws SQLException {
        String sql = "SELECT name, stock FROM products WHERE stock > ? ORDER BY stock DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, minStock);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  %-12s  stock=%d%n",
                        rs.getString("name"), rs.getInt("stock"));
                }
            }
        }
    }
}
