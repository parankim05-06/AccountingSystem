package service;

import dao.UserDAO;
import model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Registration and login rules.
 *
 * HOW PASSWORDS ARE PROTECTED (beginner version)
 *  1. We never save the password. We save a "hash": the result of scrambling the
 *     password with a one-way maths function. You cannot turn a hash back into the
 *     password.
 *  2. A random "salt" is mixed in, so two users with the same password get
 *     different hashes.
 *  3. The function (PBKDF2) is deliberately slow, which makes guessing millions of
 *     passwords very expensive for an attacker.
 *  4. To log in we scramble the typed password with the SAME salt and check
 *     whether the result equals the saved hash.
 *
 * What is saved in users.password_hash:   iterations:salt:hash   (all text)
 */
public class AuthService {

    private static final int ITERATIONS = 120000;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserDAO userDAO = new UserDAO();

    public void register(String username, String password) throws AppException {
        username = username == null ? "" : username.trim();
        if (username.isEmpty() || password == null || password.isEmpty()) {
            throw new AppException("Fields cannot be empty.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new AppException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        try {
            if (userDAO.findByUsername(username) != null) {
                throw new AppException("Username already exists!");
            }
            userDAO.insert(username, hashPassword(password), now());
        } catch (SQLException e) {
            throw new AppException("Unable to create the account. Please try again.", e);
        }
    }

    /** Returns the logged-in user, or throws "Invalid username or password." */
    public User login(String username, String password) throws AppException {
        username = username == null ? "" : username.trim();
        if (username.isEmpty() || password == null || password.isEmpty()) {
            throw new AppException("Please enter your username and password.");
        }

        try {
            User user = userDAO.findByUsername(username);
            // The same message for "no such user" and "wrong password", so nobody
            // can use the login screen to discover which usernames exist.
            if (user == null || !passwordMatches(password, user.getPasswordHash())) {
                throw new AppException("Invalid username or password.");
            }
            return user;
        } catch (SQLException e) {
            throw new AppException("Unable to log in right now. Please try again.", e);
        }
    }

    /** Turns a password into "iterations:salt:hash". A new random salt every time. */
    public static String hashPassword(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = scramble(password, salt, ITERATIONS);
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                          + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /** True if the typed password produces the stored hash. */
    public static boolean passwordMatches(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expected = Base64.getDecoder().decode(parts[2]);
            byte[] actual = scramble(password, salt, iterations);
            return MessageDigest.isEqual(expected, actual); // compares without leaking timing info
        } catch (RuntimeException e) {
            return false; // a damaged hash never matches
        }
    }

    private static byte[] scramble(String password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing is not available on this computer", e);
        }
    }

    private static String now() {
        return LocalDateTime.now().withNano(0).toString(); // 2026-10-27T14:03:00
    }
}
