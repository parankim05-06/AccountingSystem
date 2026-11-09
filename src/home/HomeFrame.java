package home;

import auth.AuthService;
import auth.SignUpFrame;
import auth.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class HomeFrame extends JFrame {

    private final Color BLACK = new Color(20, 20, 20);
    private final Color LIGHT = new Color(245, 245, 245);
    private final Color TEXT_DARK = new Color(40, 40, 40);
    private final Color TEXT_MUTED = new Color(120, 120, 120);

    public HomeFrame() {
        this(new java.util.HashMap<>(), "users.txt");
    }

    public HomeFrame(Map<String, User> userDatabase, String fileName) {
        setTitle("Accounting System - Home");
        setSize(900, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        // Logo (icon + ENTRI)
        JLabel logo = new JLabel("\uD83D\uDCCA ENTRI");
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
            JLabel bullet = new JLabel("\u2022  ");
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

        // ===== RIGHT WELCOME PANEL =====
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(LIGHT);
        right.setBorder(new EmptyBorder(40, 50, 40, 50));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(LIGHT);

        JLabel welcomeTitle = new JLabel("Welcome");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeTitle.setForeground(TEXT_DARK);
        welcomeTitle.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel welcomeSub = new JLabel("Log in to your account or create a new one to get");
        welcomeSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeSub.setForeground(TEXT_MUTED);
        welcomeSub.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel welcomeSub2 = new JLabel("started.");
        welcomeSub2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeSub2.setForeground(TEXT_MUTED);
        welcomeSub2.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Log in button (dark, rounded)
        JButton loginBtn = new JButton("Log in");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setBackground(BLACK);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(new RoundedBorder(10));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setMaximumSize(new Dimension(360, 46));
        loginBtn.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        // Sign up button (white with border)
        JButton signUpBtn = new JButton("Sign up");
        signUpBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        signUpBtn.setBackground(LIGHT);
        signUpBtn.setForeground(TEXT_DARK);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(10, 0, 10, 0)));
        signUpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpBtn.setMaximumSize(new Dimension(360, 46));
        signUpBtn.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        form.add(welcomeTitle);
        form.add(Box.createVerticalStrut(8));
        form.add(welcomeSub);
        form.add(Box.createVerticalStrut(2));
        form.add(welcomeSub2);
        form.add(Box.createVerticalStrut(28));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(12));
        form.add(signUpBtn);

        right.add(form);

        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);
        add(root);

        // Button Actions
        loginBtn.addActionListener(e -> {
            new AuthService(userDatabase, fileName).showLoginWindow();
            dispose();
        });

        signUpBtn.addActionListener(e -> {
            new SignUpFrame(userDatabase, fileName).setVisible(true);
        });
    }

    /** A simple rounded line border for buttons. */
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius) {
            this.radius = radius;
            this.color = new Color(20, 20, 20);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(color);
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }
}
