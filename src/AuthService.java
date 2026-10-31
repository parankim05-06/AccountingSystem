package src;

import src.homepage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AuthService {
    private Map<String, User> userDatabase = new HashMap<>();
    private final String FILE_NAME = "users.txt";

    private final Color BG_COLOR = new Color(248, 246, 242);
    private final Color CARD_COLOR = new Color(255, 255, 255);
    private final Color PRIMARY_COLOR = new Color(92, 64, 51);
    private final Color TEXT_COLOR = new Color(60, 50, 45);
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 13);

    public AuthService() {
        loadUsersFromFile();
        if (userDatabase.isEmpty()) {
            userDatabase.put("admin", new User("admin", "admin123"));
            saveUsersToFile();
        }
    }

    private void loadUsersFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        userDatabase.put(parts[0], new User(parts[0], parts[1]));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading user data.");
        }
    }

    private void saveUsersToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME, false)) {
            for (User user : userDatabase.values()) {
                writer.write(user.toFileString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving user data.");
        }
    }

    public void showLoginWindow() {
        JFrame frame = new JFrame("Accounting System - Login");
        frame.setSize(450, 380);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

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

        JLabel titleLabel = new JLabel("Welcome Back", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        cardPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Sign in to access your ledger", JLabel.CENTER);
        subtitleLabel.setFont(LABEL_FONT);
        subtitleLabel.setForeground(new Color(120, 110, 105));
        gbc.gridy = 1;
        cardPanel.add(subtitleLabel, gbc);

        // Username
        gbc.gridwidth = 1; gbc.gridy = 2; gbc.gridx = 0;
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(LABEL_FONT);
        userLabel.setForeground(TEXT_COLOR);
        cardPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        cardPanel.add(userField, gbc);

        // Password
        gbc.gridy = 3; gbc.gridx = 0;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(LABEL_FONT);
        passLabel.setForeground(TEXT_COLOR);
        cardPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        cardPanel.add(passField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(CARD_COLOR);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(PRIMARY_COLOR);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);

        JButton signUpPageBtn = new JButton("Sign Up");
        signUpPageBtn.setBackground(new Color(230, 225, 218));
        signUpPageBtn.setForeground(TEXT_COLOR);
        signUpPageBtn.setFocusPainted(false);

        btnPanel.add(loginBtn);
        btnPanel.add(signUpPageBtn);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        cardPanel.add(btnPanel, gbc);

        mainPanel.add(cardPanel);
        frame.add(mainPanel);

        // Actions
        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

    if (userDatabase.containsKey(username) && userDatabase.get(username).checkPassword(password)) {
            JOptionPane.showMessageDialog(frame, "Login Successful!");
            frame.dispose();
    
        new homepage(username).showDashboard();
    } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        signUpPageBtn.addActionListener(e -> {
            new SignUpFrame(userDatabase, FILE_NAME).setVisible(true);
        });

        frame.setVisible(true);
    }
}