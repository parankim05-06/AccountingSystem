package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;

/**
 * The colors and styles of the app, kept in one place so every screen looks the same.
 * Dominant lighter aesthetic with warm chocolate card contrast.
 */
public class Theme {

    // --- Dominant Lighter Palette with Warm Chocolate Accents ---
    public static final Color BG_DARK = new Color(245, 241, 235);         // dominant light background (warm cream)
    public static final Color CARD_BG = new Color(255, 253, 250);         // card background (pure warm white)
    public static final Color CARD_LIGHT = new Color(238, 232, 224);      // hover / table header
    public static final Color ACCENT_GOLD = new Color(110, 65, 45);       // deep rich brown for primary actions
    public static final Color TEXT_WHITE = new Color(50, 35, 30);         // dark text for readability on light BG
    public static final Color TEXT_MUTED = new Color(110, 95, 85);        // secondary text
    public static final Color BORDER_DARK = new Color(210, 195, 180);     // soft borders

    // Backward compatibility mappings
    public static final Color WARM_CREAM = BG_DARK;
    public static final Color SOFT_CARD = CARD_BG;
    public static final Color WARM_BEIGE = ACCENT_GOLD;
    public static final Color DEEP_MOCHA = ACCENT_GOLD;
    public static final Color TEXT_DARK = TEXT_WHITE;
    public static final Color CARD_BORDER = BORDER_DARK;
    public static final Color SUBTLE_TEXT = TEXT_MUTED;
    public static final Color LOGOUT_BUTTON = new Color(175, 105, 95);
    public static final Color NEW_JOURNAL_BUTTON = new Color(238, 232, 224);

    public static final String DASHBOARD_FONT = "Segoe UI";

    /** A rounded border for cards. */
    public static javax.swing.border.Border roundedBorder(int thickness) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_DARK, thickness, true),
                new EmptyBorder(14, 16, 14, 16));
    }

    /** A card panel with light background and rounded border. */
    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(roundedBorder(1));
        return p;
    }

    /** Rich brown button with white text (the main action on a screen). */
    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(92, 54, 41));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(new MatteBorder(1, 1, 1, 1, new Color(70, 40, 30)));
        return button;
    }

    /** Lighter button for secondary actions. */
    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(230, 220, 210));
        button.setForeground(new Color(60, 40, 35));
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(new MatteBorder(1, 1, 1, 1, new Color(190, 175, 160)));
        return button;
    }

    /** A styled label for section headings. */
    public static JLabel headingLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(new Color(92, 54, 41));
        return label;
    }

    /** A styled label for body text. */
    public static JLabel bodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_WHITE);
        return label;
    }

    /**
     * Shows your logo if the file resources/logo.png exists, otherwise returns null.
     */
    public static JLabel logoLabel() {
        File logoFile = new File("resources", "logo.png");
        if (!logoFile.isFile()) {
            return null;
        }
        return new JLabel(new ImageIcon(logoFile.getPath()), JLabel.CENTER);
    }

    /**
     * A table model whose cells cannot be edited by double-clicking them.
     */
    public static DefaultTableModel readOnlyTableModel(String[] columnNames) {
        return new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }
}