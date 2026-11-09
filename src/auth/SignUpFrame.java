package auth;

import ui.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SignUpFrame extends JFrame {
    private Map<String, User> userDatabase;
    private final String FILE_NAME;

    private JTextField userField;
    private JPasswordField passField;

    public SignUpFrame(Map<String, User> userDatabase, String fileName) {
        this.userDatabase = userDatabase;
        this.FILE_NAME = fileName;

        setTitle("Accounting System - Create Account");
        setSize(420, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.BG_DARK);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Theme.CARD_BG);
        cardPanel.setBorder(new MatteBorder(1, 1, 1, 1, Theme.BORDER_DARK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create Account", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Theme.ACCENT_GOLD);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        cardPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        JLabel userLabel = new JLabel("New Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setBackground(Theme.TEXT_WHITE);
        cardPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        userField = new JTextField(15);
        cardPanel.add(userField, gbc);

        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("New Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passLabel.setBackground(Theme.TEXT_WHITE);
        cardPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        passField = new JPasswordField(15);
        cardPanel.add(passField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(Theme.CARD_BG);

        JButton registerBtn = Theme.primaryButton("Register");
        JButton backBtn = Theme.secondaryButton("Back to Login");

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        cardPanel.add(btnPanel, gbc);

        mainPanel.add(cardPanel);
        add(mainPanel);

        // Actions
        registerBtn.addActionListener(e -> handleRegistration());
        backBtn.addActionListener(e -> dispose());
    }

    private void handleRegistration() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password must be at least 8 characters long.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userDatabase.containsKey(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        userDatabase.put(username, new User(username, password));
        saveUsersToFile();
        JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.");
        dispose();
    }

    private void saveUsersToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME, false)) {
            for (User user : userDatabase.values()) {
                writer.write(user.toFileString() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving user data.");
        }
    }
}