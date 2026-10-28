package ui;

import model.User;
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

/** Login screen. It only collects what you type; AuthService decides if it is correct. */
public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);

    public LoginFrame() {
        setTitle("Accounting System - Login");
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel logo = Theme.logoLabel();
        if (logo != null) {
            cardPanel.add(logo, gbc);
            gbc.gridy++;
        }

        JLabel titleLabel = new JLabel("Welcome Back", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(Theme.DEEP_MOCHA);
        cardPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("Sign in to access your ledger", JLabel.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(Theme.SUBTLE_TEXT);
        gbc.gridy++;
        cardPanel.add(subtitleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        cardPanel.add(makeLabel("Username"), gbc);
        gbc.gridx = 1;
        cardPanel.add(userField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        cardPanel.add(makeLabel("Password"), gbc);
        gbc.gridx = 1;
        cardPanel.add(passField, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        JButton loginButton = Theme.primaryButton("Login");
        JButton signUpButton = Theme.secondaryButton("Sign Up");
        buttonPanel.add(loginButton);
        buttonPanel.add(signUpButton);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        cardPanel.add(buttonPanel, gbc);

        mainPanel.add(cardPanel);
        add(mainPanel);

        getRootPane().setDefaultButton(loginButton); // pressing Enter clicks Login
        loginButton.addActionListener(e -> handleLogin());
        signUpButton.addActionListener(e -> new SignUpFrame().setVisible(true));
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 13));
        label.setForeground(Theme.TEXT_DARK);
        return label;
    }

    private void handleLogin() {
        try {
            User user = authService.login(userField.getText(), new String(passField.getPassword()));
            JOptionPane.showMessageDialog(this, "Login Successful!");
            dispose();
            new HomeFrame(user).setVisible(true);
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
