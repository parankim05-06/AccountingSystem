package dao;

import database.Database;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** SQL for the users table. DAOs only talk to the database; they make no decisions. */
public class UserDAO {

    /** Returns the user, or null if no user has that username. */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username); // the ? is filled in safely (stops SQL injection)
            try (ResultSet row = statement.executeQuery()) {
                if (row.next()) {
                    return new User(row.getInt("id"), row.getString("username"),
                                    row.getString("password_hash"), row.getString("created_at"));
                }
                return null;
            }
        }
    }

    public void insert(String username, String passwordHash, String createdAt) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, created_at) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, createdAt);
            statement.executeUpdate();
        }
    }
}
