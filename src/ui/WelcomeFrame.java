package ui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** First screen (was HomeFrame in the old project): LOG-IN and SIGN-UP buttons. */
public class WelcomeFrame extends JFrame {

    public WelcomeFrame() {
        setTitle("Accounting System - Home");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Theme.WARM_CREAM);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.CARD_BORDER, 1),
                new EmptyBorder(30, 30, 30, 30)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel logo = Theme.logoLabel();
        if (logo != null) {
            cardPanel.add(logo, gbc);
            gbc.gridy++;
        }

        JLabel titleLabel = new JLabel("Accounting System", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Theme.DEEP_MOCHA);
        cardPanel.add(titleLabel, gbc);

        JButton loginButton = Theme.primaryButton("LOG-IN");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy++;
        cardPanel.add(loginButton, gbc);

        JButton signUpButton = Theme.secondaryButton("SIGN-UP");
        signUpButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridy++;
        cardPanel.add(signUpButton, gbc);

        mainPanel.add(cardPanel);
        add(mainPanel);

        loginButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        signUpButton.addActionListener(e -> new SignUpFrame().setVisible(true));
    }
}
