package auth;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SignUpFrame extends JFrame {
    private Map<String, User> userDatabase;
    private final String FILE_NAME;

    private JTextField userField;
    private JPasswordField passField;

    private final Color BG_COLOR = new Color(248, 246, 242);
    private final Color CARD_COLOR = new Color(255, 255, 255);
    private final Color PRIMARY_COLOR = new Color(92, 64, 51);
    private final Color TEXT_COLOR = new Color(60, 50, 45);
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);

    public SignUpFrame(Map<String, User> userDatabase, String fileName) {
        this.userDatabase = userDatabase;
        this.FILE_NAME = fileName;

        setTitle("Accounting System - Create Account");
        setSize(420, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(CARD_COLOR);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 215, 210), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create Account", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        cardPanel.add(titleLabel, gbc);

        // Username
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        JLabel userLabel = new JLabel("New Username");
        userLabel.setFont(LABEL_FONT);
        userLabel.setForeground(TEXT_COLOR);
        cardPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        userField = new JTextField(15);
        cardPanel.add(userField, gbc);

        // Password
        gbc.gridy = 2; gbc.gridx = 0;
        JLabel passLabel = new JLabel("New Password");
        passLabel.setFont(LABEL_FONT);
        passLabel.setForeground(TEXT_COLOR);
        cardPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        passField = new JPasswordField(15);
        cardPanel.add(passField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(CARD_COLOR);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBackground(PRIMARY_COLOR);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);

        JButton backBtn = new JButton("Back to Login");
        backBtn.setBackground(new Color(230, 225, 218));
        backBtn.setForeground(TEXT_COLOR);
        backBtn.setFocusPainted(false);

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