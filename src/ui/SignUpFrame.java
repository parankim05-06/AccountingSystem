package ui;

import service.AppException;
import service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Sign up screen. Split-screen layout: left dark branding panel, right light registration form.
 */
public class SignUpFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final JTextField userField = new JTextField(20);
    private final JPasswordField passField = new JPasswordField(20);

    private static final Color BLACK = new Color(20, 20, 20);
    private static final Color LIGHT = new Color(245, 245, 245);
    private static final Color INPUT_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(40, 40, 40);
    private static final Color TEXT_MUTED = new Color(120, 120, 120);

    public SignUpFrame() {
        setTitle("Accounting System - Sign Up");
        setSize(900, 560);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(LIGHT);

        // ===== LEFT BRANDING PANEL =====
        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(BLACK);

        JPanel leftContent = new JPanel();
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.setBackground(BLACK);
        leftContent.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Logo
        JLabel logo = new JLabel("📋 ENTRI");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Title
        JLabel title = new JLabel("Accounting System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Subtitle
        JLabel subtitle = new JLabel("Clear, simple bookkeeping for your business.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitle.setForeground(new Color(180, 180, 180));
        subtitle.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Feature list
        JPanel features = new JPanel();
        features.setLayout(new BoxLayout(features, BoxLayout.Y_AXIS));
        features.setBackground(BLACK);
        features.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        String[] items = {"Journal entries", "Invoices and bills", "Financial reports"};
        for (String item : items) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            row.setBackground(BLACK);
            JLabel bullet = new JLabel("•  ");
            bullet.setFont(new Font("Segoe UI", Font.BOLD, 16));
            bullet.setForeground(new Color(200, 200, 200));
            JLabel txt = new JLabel(item);
            txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            txt.setForeground(new Color(190, 190, 190));
            row.add(bullet);
            row.add(txt);
            features.add(row);
            features.add(Box.createVerticalStrut(6));
        }

        leftContent.add(logo);
        leftContent.add(Box.createVerticalStrut(90));
        leftContent.add(title);
        leftContent.add(Box.createVerticalStrut(12));
        leftContent.add(subtitle);
        leftContent.add(Box.createVerticalStrut(60));
        leftContent.add(features);

        left.add(leftContent);

        // ===== RIGHT SIGN-UP PANEL =====
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(LIGHT);
        right.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(LIGHT);

        JLabel signUpTitle = new JLabel("Create account");
        signUpTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        signUpTitle.setForeground(TEXT_DARK);
        signUpTitle.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel signUpSub = new JLabel("Enter your details to get started.");
        signUpSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        signUpSub.setForeground(TEXT_MUTED);
        signUpSub.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(TEXT_DARK);
        userLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userField.setMaximumSize(new Dimension(360, 40));
        userField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
                new EmptyBorder(8, 12, 8, 12)));
        userField.setBackground(INPUT_BG);
        userField.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passLabel.setForeground(TEXT_DARK);
        passLabel.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        passField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passField.setMaximumSize(new Dimension(360, 40));
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
                new EmptyBorder(8, 12, 8, 12)));
        passField.setBackground(INPUT_BG);
        passField.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Register button
        JButton registerBtn = new JButton("Sign up");
        registerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        registerBtn.setBackground(BLACK);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setMaximumSize(new Dimension(360, 42));
        registerBtn.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Login link
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        linkPanel.setBackground(LIGHT);
        linkPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        JLabel hasAcc = new JLabel("Already have an account?");
        hasAcc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hasAcc.setForeground(TEXT_MUTED);
        JButton loginLinkBtn = new JButton("Log in");
        loginLinkBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginLinkBtn.setBackground(LIGHT);
        loginLinkBtn.setForeground(Color.BLUE);
        loginLinkBtn.setFocusPainted(false);
        loginLinkBtn.setBorder(null);
        loginLinkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkPanel.add(hasAcc);
        linkPanel.add(loginLinkBtn);

        form.add(signUpTitle);
        form.add(Box.createVerticalStrut(6));
        form.add(signUpSub);
        form.add(Box.createVerticalStrut(28));
        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(userField);
        form.add(Box.createVerticalStrut(16));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passField);
        form.add(Box.createVerticalStrut(24));
        form.add(registerBtn);
        form.add(Box.createVerticalStrut(16));
        form.add(linkPanel);

        right.add(form);

        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        add(root);

        getRootPane().setDefaultButton(registerBtn);
        registerBtn.addActionListener(e -> handleRegistration());
        loginLinkBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void handleRegistration() {
        try {
            authService.register(userField.getText(), new String(passField.getPassword()));
            JOptionPane.showMessageDialog(this, "Account created successfully! You can now log in.");
            new LoginFrame().setVisible(true);
            dispose();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
