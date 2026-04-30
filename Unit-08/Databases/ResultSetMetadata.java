import java.sql.*;

// Demonstrates ResultSetMetaData and DatabaseMetaData — the JDBC reflection APIs.
//
// ResultSetMetaData describes the *columns* of a query result at runtime:
//   column count, names, SQL types, display widths, nullability, etc.
//   Useful for generic table-printing utilities, CSV exporters, or ORM frameworks
//   that need to map query results to objects without knowing the schema in advance.
//
// DatabaseMetaData describes the *database itself*:
//   product name/version, supported features, list of tables, column metadata, etc.
//   Useful for schema-migration tools, database browsers, and compatibility checks.
public class ResultSetMetadata {

    // ⚠ WARNING: Never hardcode credentials in real applications.
    private static final String URL  = "jdbc:postgresql://localhost:5432/lab42";
    private static final String USER = "matthewconroy";
    private static final String PASS = "password";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            setupSchema(conn);
            inspectQueryColumns(conn);
            printTableGeneric(conn, "SELECT * FROM catalog ORDER BY id");
            inspectDatabaseInfo(conn);
            listTables(conn);
        }
    }

    private static void setupSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS catalog (
                    id          SERIAL PRIMARY KEY,
                    name        TEXT           NOT NULL,
                    price       NUMERIC(10, 2),
                    in_stock    BOOLEAN        NOT NULL DEFAULT TRUE
                )""");
            st.execute("DELETE FROM catalog");
            st.execute("INSERT INTO catalog (name, price, in_stock) VALUES ('Alpha', 9.99, TRUE)");
            st.execute("INSERT INTO catalog (name, price, in_stock) VALUES ('Beta', NULL, FALSE)");
            st.execute("INSERT INTO catalog (name, price, in_stock) VALUES ('Gamma', 49.00, TRUE)");
        }
    }

    // Use ResultSetMetaData to print column names, types, and nullability.
    private static void inspectQueryColumns(Connection conn) throws SQLException {
        System.out.println("=== Column Metadata ===");
        String sql = "SELECT * FROM catalog ORDER BY id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            System.out.printf("Query has %d column(s):%n", cols);
            for (int c = 1; c <= cols; c++) {
                System.out.printf("  col %-2d  name=%-14s  sql_type=%-12s  nullable=%s%n",
                    c,
                    meta.getColumnName(c),
                    meta.getColumnTypeName(c),
                    meta.isNullable(c) == ResultSetMetaData.columnNoNulls ? "NO" : "YES");
            }
        }
        System.out.println();
    }

    // Generic table printer — works with any SELECT query.
    private static void printTableGeneric(Connection conn, String sql) throws SQLException {
        System.out.println("=== Generic Table Print ===");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            // Header row
            for (int c = 1; c <= cols; c++) {
                System.out.printf("%-15s", meta.getColumnName(c).toUpperCase());
            }
            System.out.println();
            System.out.println("-".repeat(cols * 15));

            // Data rows
            while (rs.next()) {
                for (int c = 1; c <= cols; c++) {
                    Object val = rs.getObject(c);
                    System.out.printf("%-15s", val == null ? "NULL" : val.toString());
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    // Database-level metadata: server version, JDBC driver info.
    private static void inspectDatabaseInfo(Connection conn) throws SQLException {
        System.out.println("=== Database Metadata ===");
        DatabaseMetaData dbMeta = conn.getMetaData();
        System.out.println("  Product   : " + dbMeta.getDatabaseProductName()
                           + " " + dbMeta.getDatabaseProductVersion());
        System.out.println("  Driver    : " + dbMeta.getDriverName()
                           + " " + dbMeta.getDriverVersion());
        System.out.println("  URL       : " + dbMeta.getURL());
        System.out.println("  Max conns : " + dbMeta.getMaxConnections());
        System.out.println("  Supports transactions : " + dbMeta.supportsTransactions());
        System.out.println("  Supports batch updates: " + dbMeta.supportsBatchUpdates());
        System.out.println();
    }

    // List all user-created tables in the current schema.
    private static void listTables(Connection conn) throws SQLException {
        System.out.println("=== Tables in Schema ===");
        DatabaseMetaData dbMeta = conn.getMetaData();
        // getCatalog() / getSchema() may return null on some drivers; use null to mean "all".
        try (ResultSet rs = dbMeta.getTables(null, "public", "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                System.out.printf("  %-30s  type=%s%n",
                    rs.getString("TABLE_NAME"),
                    rs.getString("TABLE_TYPE"));
            }
        }
    }
}
