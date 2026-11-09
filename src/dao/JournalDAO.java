package dao;

import database.Database;
import model.JournalEntry;
import model.JournalLine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** SQL for journal_entries and journal_lines. */
public class JournalDAO {

    // Columns we read for a line, joined with its account (used by several queries).
    private static final String LINE_COLUMNS =
        "l.id, l.journal_entry_id, l.account_id, l.debit, l.credit, "
      + "a.account_code, a.account_name, a.account_type";

    /**
     * Saves an entry AND all its lines as ONE all-or-nothing unit (a transaction).
     * If anything fails halfway, rollback() undoes everything, so the database
     * never contains an entry without its lines.
     */
    public int insert(JournalEntry entry) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            connection.setAutoCommit(false); // start a transaction: nothing is final yet
            try {
                int entryId = insertEntryRow(connection, entry);
                insertLineRows(connection, entryId, entry);
                connection.commit();         // everything worked: make it permanent
                return entryId;
            } catch (SQLException e) {
                connection.rollback();       // something failed: undo everything
                throw e;
            }
        }
    }

    private int insertEntryRow(Connection connection, JournalEntry entry) throws SQLException {
        String sql = "INSERT INTO journal_entries (entry_date, description, status, created_at) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entry.getEntryDate());
            statement.setString(2, entry.getDescription());
            statement.setString(3, entry.getStatus());
            statement.setString(4, entry.getCreatedAt());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1); // the id SQLite gave the new entry
            }
        }
    }

    private void insertLineRows(Connection connection, int entryId, JournalEntry entry) throws SQLException {
        String sql = "INSERT INTO journal_lines (journal_entry_id, account_id, debit, credit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (JournalLine line : entry.getLines()) {
                statement.setInt(1, entryId);
                statement.setInt(2, line.getAccountId());
                statement.setLong(3, line.getDebit());
                statement.setLong(4, line.getCredit());
                statement.executeUpdate();
            }
        }
    }

    /** One entry with its lines, or null if there is no entry with that id. */
    public JournalEntry findById(int entryId) throws SQLException {
        String sql = "SELECT id, entry_date, description, status, created_at FROM journal_entries WHERE id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entryId);
            try (ResultSet row = statement.executeQuery()) {
                if (!row.next()) return null;
                JournalEntry entry = readEntry(row);
                entry.getLines().addAll(findLines(connection, entry.getId()));
                return entry;
            }
        }
    }

    /** Every entry (oldest first) with its lines. */
    public List<JournalEntry> findAll() throws SQLException {
        String sql = "SELECT id, entry_date, description, status, created_at FROM journal_entries ORDER BY id";
        List<JournalEntry> entries = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery(sql)) {
            while (row.next()) {
                entries.add(readEntry(row));
            }
            // One extra small query per entry. Fine for a small project, easy to read.
            for (JournalEntry entry : entries) {
                entry.getLines().addAll(findLines(connection, entry.getId()));
            }
        }
        return entries;
    }

    /** Changes DRAFT to POSTED. Returns how many rows changed (0 means it was not a draft). */
    public int markPosted(int entryId) throws SQLException {
        String sql = "UPDATE journal_entries SET status = 'POSTED' WHERE id = ? AND status = 'DRAFT'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entryId);
            return statement.executeUpdate();
        }
    }

    /**
     * Every line of every POSTED entry, with account and entry info, sorted by
     * account then date. The ledger groups these by account.
     */
    public List<JournalLine> findPostedLines() throws SQLException {
        String sql = "SELECT " + LINE_COLUMNS + ", e.entry_date, e.description "
                   + "FROM journal_lines l "
                   + "JOIN accounts a        ON a.id = l.account_id "
                   + "JOIN journal_entries e ON e.id = l.journal_entry_id "
                   + "WHERE e.status = 'POSTED' "
                   + "ORDER BY a.account_code, e.entry_date, e.id, l.id";
        List<JournalLine> lines = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery(sql)) {
            while (row.next()) {
                JournalLine line = readLine(row);
                line.setEntryInfo(row.getString("entry_date"), row.getString("description"));
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * One row per account that has posted lines: the SUM of its debits and the SUM
     * of its credits. (We reuse JournalLine to carry these two totals.)
     */
    public List<JournalLine> findPostedTotalsByAccount() throws SQLException {
        String sql = "SELECT a.id AS account_id, a.account_code, a.account_name, a.account_type, "
                   + "       SUM(l.debit) AS total_debit, SUM(l.credit) AS total_credit "
                   + "FROM journal_lines l "
                   + "JOIN accounts a        ON a.id = l.account_id "
                   + "JOIN journal_entries e ON e.id = l.journal_entry_id "
                   + "WHERE e.status = 'POSTED' "
                   + "GROUP BY a.id, a.account_code, a.account_name, a.account_type "
                   + "ORDER BY a.account_code";
        List<JournalLine> totals = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery(sql)) {
            while (row.next()) {
                JournalLine total = new JournalLine(row.getInt("account_id"),
                                                    row.getLong("total_debit"), row.getLong("total_credit"));
                total.setAccountInfo(row.getString("account_code"), row.getString("account_name"),
                                     row.getString("account_type"));
                totals.add(total);
            }
        }
        return totals;
    }

    // ---- helpers that turn one database row into a Java object ----

    private List<JournalLine> findLines(Connection connection, int entryId) throws SQLException {
        String sql = "SELECT " + LINE_COLUMNS + " FROM journal_lines l "
                   + "JOIN accounts a ON a.id = l.account_id "
                   + "WHERE l.journal_entry_id = ? ORDER BY l.id";
        List<JournalLine> lines = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, entryId);
            try (ResultSet row = statement.executeQuery()) {
                while (row.next()) {
                    lines.add(readLine(row));
                }
            }
        }
        return lines;
    }

    private JournalEntry readEntry(ResultSet row) throws SQLException {
        JournalEntry entry = new JournalEntry();
        entry.setId(row.getInt("id"));
        entry.setEntryDate(row.getString("entry_date"));
        entry.setDescription(row.getString("description"));
        entry.setStatus(row.getString("status"));
        entry.setCreatedAt(row.getString("created_at"));
        return entry;
    }

    private JournalLine readLine(ResultSet row) throws SQLException {
        JournalLine line = new JournalLine(row.getInt("account_id"), row.getLong("debit"), row.getLong("credit"));
        line.setId(row.getInt("id"));
        line.setJournalEntryId(row.getInt("journal_entry_id"));
        line.setAccountInfo(row.getString("account_code"), row.getString("account_name"),
                            row.getString("account_type"));
        return line;
    }
}
