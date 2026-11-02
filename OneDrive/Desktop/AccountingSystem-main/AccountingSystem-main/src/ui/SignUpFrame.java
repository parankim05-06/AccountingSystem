package ui;

import service.AppException;
import service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

/** "Create Account" window. AuthService checks the input and saves the user. */
public class SignUpFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);

    public SignUpFrame() {
        setTitle("Accounting System - Create Account");
        setSize(420, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.WARM_CREAM);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.CARD_BORDER, 1),
                new EmptyBorder(25, 25, 25, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create Account", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Theme.DEEP_MOCHA);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        cardPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        cardPanel.add(makeLabel("New Username"), gbc);
        gbc.gridx = 1;
        cardPanel.add(userField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        cardPanel.add(makeLabel("New Password"), gbc);
        gbc.gridx = 1;
        cardPanel.add(passField, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton registerButton = Theme.primaryButton("Register");
        JButton backButton = Theme.secondaryButton("Back to Login");
        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        cardPanel.add(buttonPanel, gbc);

        mainPanel.add(cardPanel);
        add(mainPanel);

        getRootPane().setDefaultButton(registerButton);
        registerButton.addActionListener(e -> handleRegistration());
        backButton.addActionListener(e -> dispose());
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(Theme.TEXT_DARK);
        return label;
    }

    private void handleRegistration() {
        try {
            authService.register(userField.getText(), new String(passField.getPassword()));
            JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.");
            dispose();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
