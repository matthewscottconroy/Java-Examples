import java.sql.*;

// Demonstrates ACID transactions: commit, rollback, and savepoints.
//
// By default, JDBC operates in auto-commit mode — each statement is its own
// transaction.  For operations that must succeed or fail as a unit (e.g. a
// bank transfer: debit one account AND credit another), disable auto-commit
// and manage the transaction boundary manually.
//
//   conn.setAutoCommit(false)  — start manual transaction management
//   conn.commit()              — make all changes permanent
//   conn.rollback()            — undo all changes since the last commit
//   conn.rollback(savepoint)   — undo back to a specific savepoint
//   Savepoint sp = conn.setSavepoint("name") — mark a rollback target
public class Transactions {

    // ⚠ WARNING: Never hardcode credentials in real applications.
    private static final String URL  = "jdbc:postgresql://localhost:5432/lab42";
    private static final String USER = "matthewconroy";
    private static final String PASS = "password";

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            setupSchema(conn);
            demonstrateCommit(conn);
            demonstrateRollback(conn);
            demonstrateSavepoint(conn);
        }
    }

    private static void setupSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id      SERIAL PRIMARY KEY,
                    owner   TEXT           NOT NULL,
                    balance NUMERIC(12, 2) NOT NULL CHECK (balance >= 0)
                )""");
            st.execute("DELETE FROM accounts");
            st.execute("INSERT INTO accounts (owner, balance) VALUES ('Alice', 1000.00)");
            st.execute("INSERT INTO accounts (owner, balance) VALUES ('Bob',    500.00)");
        }
        System.out.println("Accounts initialised: Alice=$1000, Bob=$500\n");
    }

    // Successful transfer: both sides complete, then commit.
    private static void demonstrateCommit(Connection conn) throws SQLException {
        System.out.println("--- Successful transfer: Alice → Bob $200 ---");
        conn.setAutoCommit(false);
        try {
            debit(conn,  "Alice", 200.00);
            credit(conn, "Bob",   200.00);
            conn.commit();
            System.out.println("  Committed.");
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("  Rolled back: " + e.getMessage());
        } finally {
            conn.setAutoCommit(true);
        }
        printBalances(conn);
    }

    // Failed transfer: the credit step causes a constraint violation (negative balance
    // attempt is simulated by transferring more than Alice has).
    private static void demonstrateRollback(Connection conn) throws SQLException {
        System.out.println("\n--- Failed transfer: Alice → Bob $5000 (insufficient funds) ---");
        conn.setAutoCommit(false);
        try {
            debit(conn,  "Alice", 5000.00);  // violates CHECK (balance >= 0)
            credit(conn, "Bob",   5000.00);
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            System.out.println("  Rolled back: " + e.getMessage().trim());
        } finally {
            conn.setAutoCommit(true);
        }
        printBalances(conn);
    }

    // Savepoint: perform two deposits, roll back only the second.
    private static void demonstrateSavepoint(Connection conn) throws SQLException {
        System.out.println("\n--- Savepoint demo: two deposits, rollback second ---");
        conn.setAutoCommit(false);
        try {
            credit(conn, "Alice", 50.00);
            System.out.println("  Deposited $50 into Alice.");

            Savepoint sp = conn.setSavepoint("after_first_deposit");

            credit(conn, "Bob", 300.00);
            System.out.println("  Deposited $300 into Bob — will be rolled back.");

            conn.rollback(sp);
            System.out.println("  Rolled back to savepoint (Bob's deposit undone).");

            conn.commit();
            System.out.println("  Committed (only Alice's deposit persists).");
        } catch (SQLException e) {
            conn.rollback();
        } finally {
            conn.setAutoCommit(true);
        }
        printBalances(conn);
    }

    private static void debit(Connection conn, String owner, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance - ? WHERE owner = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, owner);
            ps.executeUpdate();
        }
    }

    private static void credit(Connection conn, String owner, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE owner = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, owner);
            ps.executeUpdate();
        }
    }

    private static void printBalances(Connection conn) throws SQLException {
        String sql = "SELECT owner, balance FROM accounts ORDER BY owner";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.println("  Current balances:");
            while (rs.next()) {
                System.out.printf("    %-8s $%.2f%n",
                    rs.getString("owner"), rs.getDouble("balance"));
            }
        }
    }
}
