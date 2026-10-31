package home;

import auth.AuthService;
import auth.SignUpFrame;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;
import auth.User;

public class HomeFrame extends JFrame {
    private final Color BG_COLOR = new Color(248, 246, 242);       // Warm / Light Cream
    private final Color CARD_COLOR = new Color(255, 255, 255);
    private final Color PRIMARY_COLOR = new Color(92, 64, 51);     // Espresso Brown
    private final Color TEXT_COLOR = new Color(60, 50, 45);

    public HomeFrame(Map<String, User> userDatabase, String fileName) {
        setTitle("Accounting System - Home");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BG_COLOR);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(CARD_COLOR);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 215, 210), 1),
            new EmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Accounting System", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        cardPanel.add(titleLabel, gbc);

        // LOG-IN Button (Wireframe #1)
        JButton loginNavBtn = new JButton("LOG-IN");
        loginNavBtn.setBackground(PRIMARY_COLOR);
        loginNavBtn.setForeground(Color.WHITE);
        loginNavBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        loginNavBtn.setFocusPainted(false);
        gbc.gridy = 1;
        cardPanel.add(loginNavBtn, gbc);

        // SIGN-UP Button (Wireframe #1)
        JButton signUpNavBtn = new JButton("SIGN-UP");
        signUpNavBtn.setBackground(new Color(230, 225, 218));
        signUpNavBtn.setForeground(TEXT_COLOR);
        signUpNavBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        signUpNavBtn.setFocusPainted(false);
        gbc.gridy = 2;
        cardPanel.add(signUpNavBtn, gbc);

        mainPanel.add(cardPanel);
        add(mainPanel);

        // Button Actions linking to wireframe screens #2
        loginNavBtn.addActionListener(e -> {
            new AuthService().showLoginWindow();
            dispose();
        });

        signUpNavBtn.addActionListener(e -> {
            new SignUpFrame(userDatabase, fileName).setVisible(true);
        });
    }
}