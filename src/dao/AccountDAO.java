package dao;

import database.Database;
import model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** SQL for the accounts table (the chart of accounts). */
public class AccountDAO {

    /** All accounts that are switched on, in account-code order. */
    public List<Account> findActive() throws SQLException {
        String sql = "SELECT id, account_code, account_name, account_type "
                   + "FROM accounts WHERE is_active = 1 ORDER BY account_code";
        List<Account> accounts = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet row = statement.executeQuery(sql)) {
            while (row.next()) {
                accounts.add(readAccount(row));
            }
        }
        return accounts;
    }

    /** Finds an account by name, ignoring upper/lower case. Returns null if none. */
    public Account findByName(String name) throws SQLException {
        String sql = "SELECT id, account_code, account_name, account_type "
                   + "FROM accounts WHERE account_name = ? COLLATE NOCASE";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet row = statement.executeQuery()) {
                return row.next() ? readAccount(row) : null;
            }
        }
    }

    /** Adds an account and returns its new id. */
    public int insert(String code, String name, String type) throws SQLException {
        String sql = "INSERT INTO accounts (account_code, account_name, account_type) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, type);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /**
     * Suggests the next free account code for a type, e.g. the next Asset code
     * after 1500 is 1510. Used when old data mentions an account we do not have.
     */
    public String nextCodeForType(String type) throws SQLException {
        int firstDigit = 1; // Asset
        for (int i = 0; i < Account.TYPES.length; i++) {
            if (Account.TYPES[i].equals(type)) firstDigit = i + 1;
        }
        String sql = "SELECT MAX(CAST(account_code AS INTEGER)) FROM accounts WHERE account_type = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type);
            try (ResultSet row = statement.executeQuery()) {
                row.next();
                int highest = row.getInt(1); // 0 when the type has no accounts yet
                return String.valueOf(highest == 0 ? firstDigit * 1000 : highest + 10);
            }
        }
    }

    private Account readAccount(ResultSet row) throws SQLException {
        return new Account(row.getInt("id"), row.getString("account_code"),
                           row.getString("account_name"), row.getString("account_type"));
    }
}
