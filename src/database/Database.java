package database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The ONE class that knows where the database file is and how to open it.
 *
 * Every DAO calls Database.getConnection() when it needs to talk to SQLite.
 * Database.initialize() runs once at startup: it creates the tables if they do
 * not exist yet and fills the starter chart of accounts.
 */
public class Database {

    private static String databasePath;

    /**
     * Where the database file lives. It is always the same place no matter which
     * folder the program is started from:
     *   Windows: C:\Users\<you>\DAPAS\dapas.db
     * (Tests can choose another file with setDatabasePath.)
     */
    public static String getDatabasePath() {
        if (databasePath == null) {
            File folder = new File(System.getProperty("user.home"), "DAPAS");
            databasePath = new File(folder, "dapas.db").getAbsolutePath();
        }
        return databasePath;
    }

    public static void setDatabasePath(String path) {
        databasePath = path;
    }

    /** Opens a NEW connection. Callers close it with try-with-resources. */
    public static Connection getConnection() throws SQLException {
        File parentFolder = new File(getDatabasePath()).getParentFile();
        if (parentFolder != null) {
            parentFolder.mkdirs(); // create the DAPAS folder the first time
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + getDatabasePath());

        // SQLite ignores foreign keys unless you switch them on for EACH connection.
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /** Creates missing tables and the starter chart of accounts. Safe to run every startup. */
    public static void initialize() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(
                "CREATE TABLE IF NOT EXISTS users ("
              + " id            INTEGER PRIMARY KEY,"
              + " username      TEXT UNIQUE NOT NULL,"
              + " password_hash TEXT NOT NULL,"
              + " created_at    TEXT NOT NULL)");

            statement.execute(
                "CREATE TABLE IF NOT EXISTS accounts ("
              + " id           INTEGER PRIMARY KEY,"
              + " account_code TEXT UNIQUE NOT NULL,"
              + " account_name TEXT NOT NULL,"
              + " account_type TEXT NOT NULL"
              + "   CHECK (account_type IN ('Asset','Liability','Equity','Revenue','Expense')),"
              + " is_active    INTEGER NOT NULL DEFAULT 1)");

            statement.execute(
                "CREATE TABLE IF NOT EXISTS journal_entries ("
              + " id          INTEGER PRIMARY KEY,"
              + " entry_date  TEXT NOT NULL,"
              + " description TEXT NOT NULL,"
              + " status      TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','POSTED')),"
              + " created_at  TEXT NOT NULL)");

            // debit and credit are stored in CENTAVOS (whole numbers).
            // The CHECK says: no negatives, and a line is a debit OR a credit, not both.
            statement.execute(
                "CREATE TABLE IF NOT EXISTS journal_lines ("
              + " id               INTEGER PRIMARY KEY,"
              + " journal_entry_id INTEGER NOT NULL,"
              + " account_id       INTEGER NOT NULL,"
              + " debit            NUMERIC NOT NULL DEFAULT 0,"
              + " credit           NUMERIC NOT NULL DEFAULT 0,"
              + " CHECK (debit >= 0 AND credit >= 0 AND (debit = 0 OR credit = 0)),"
              + " FOREIGN KEY (journal_entry_id) REFERENCES journal_entries(id),"
              + " FOREIGN KEY (account_id)       REFERENCES accounts(id))");

            addStarterAccountsIfEmpty(connection);
        }
    }

    /** The small starter chart of accounts. Added only when the accounts table is empty. */
    private static final String[][] STARTER_ACCOUNTS = {
        {"1000", "Cash",                 "Asset"},
        {"1100", "Accounts Receivable",  "Asset"},
        {"1200", "Supplies",             "Asset"},
        {"1500", "Equipment",            "Asset"},
        {"2000", "Accounts Payable",     "Liability"},
        {"2100", "Notes Payable",        "Liability"},
        {"3000", "Owner's Capital",      "Equity"},
        {"3100", "Owner's Drawings",     "Equity"},
        {"4000", "Service Revenue",      "Revenue"},
        {"5000", "Salaries Expense",     "Expense"},
        {"5100", "Rent Expense",         "Expense"},
        {"5200", "Utilities Expense",    "Expense"},
        {"5300", "Supplies Expense",     "Expense"},
    };

    private static void addStarterAccountsIfEmpty(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM accounts")) {
            result.next();
            if (result.getInt(1) > 0) {
                return; // the user already has accounts: leave them alone
            }
        }

        String sql = "INSERT INTO accounts (account_code, account_name, account_type) VALUES (?, ?, ?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            for (String[] account : STARTER_ACCOUNTS) {
                insert.setString(1, account[0]);
                insert.setString(2, account[1]);
                insert.setString(3, account[2]);
                insert.executeUpdate();
            }
        }
    }
}
