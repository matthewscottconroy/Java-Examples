import java.sql.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// Demonstrates batch operations — sending multiple SQL statements in a single
// network round-trip instead of one per row.
//
// Why batch?
//   Inserting 10 000 rows one at a time: 10 000 round-trips to the server.
//   Inserting in batches of 500: ~20 round-trips — often 10–50× faster.
//
// How it works:
//   ps.addBatch()       — queue the current parameter set (does not send yet)
//   ps.executeBatch()   — send all queued statements and return row counts
//   ps.clearBatch()     — empty the queue (called automatically after executeBatch)
//
// Best practice: wrap the batch in a transaction.  If one row fails, the whole
// batch rolls back cleanly — no partial inserts left behind.
public class BatchOperations {

    // ⚠ WARNING: Never hardcode credentials in real applications.
    private static final String URL  = "jdbc:postgresql://localhost:5432/lab42";
    private static final String USER = "matthewconroy";
    private static final String PASS = "password";

    private static final int TOTAL_ROWS  = 10_000;
    private static final int BATCH_SIZE  = 500;

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            setupSchema(conn);
            long naive  = insertOneByOne(conn, 200);
            long batched = insertBatched(conn, TOTAL_ROWS, BATCH_SIZE);
            System.out.printf(
                "%nSummary: 200 rows one-by-one = %d ms | %d rows batched (%d/batch) = %d ms%n",
                naive, TOTAL_ROWS, BATCH_SIZE, batched);
            System.out.printf("Rows in table: %d%n", countRows(conn));
        }
    }

    private static void setupSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS measurements (
                    id        SERIAL PRIMARY KEY,
                    sensor_id INT            NOT NULL,
                    reading   NUMERIC(8, 3)  NOT NULL
                )""");
            st.execute("DELETE FROM measurements");
        }
    }

    // Baseline: individual INSERTs, each auto-committed.
    private static long insertOneByOne(Connection conn, int count) throws SQLException {
        System.out.println("Inserting " + count + " rows one-by-one...");
        String sql = "INSERT INTO measurements (sensor_id, reading) VALUES (?, ?)";
        long start = System.currentTimeMillis();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < count; i++) {
                ps.setInt(1, ThreadLocalRandom.current().nextInt(1, 11));
                ps.setDouble(2, ThreadLocalRandom.current().nextDouble(0, 100));
                ps.executeUpdate();
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("  Done in %d ms%n", elapsed);
        return elapsed;
    }

    // Batch insert in chunks — much faster over a real network connection.
    private static long insertBatched(Connection conn, int total, int batchSize)
            throws SQLException {
        System.out.printf("Inserting %d rows in batches of %d...%n", total, batchSize);
        String sql = "INSERT INTO measurements (sensor_id, reading) VALUES (?, ?)";
        long start = System.currentTimeMillis();
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < total; i++) {
                ps.setInt(1, ThreadLocalRandom.current().nextInt(1, 11));
                ps.setDouble(2, ThreadLocalRandom.current().nextDouble(0, 100));
                ps.addBatch();

                if ((i + 1) % batchSize == 0) {
                    int[] counts = ps.executeBatch();
                    System.out.printf("  Flushed batch — %d rows%n", counts.length);
                }
            }
            // Flush any remaining rows in the last partial batch.
            int[] remaining = ps.executeBatch();
            if (remaining.length > 0) {
                System.out.printf("  Flushed final batch — %d rows%n", remaining.length);
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("  Done in %d ms%n", elapsed);
        return elapsed;
    }

    private static long countRows(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM measurements")) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
