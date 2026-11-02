package ui;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.io.File;

/**
 * The colors (and two button styles) of the ORIGINAL app, kept in one place so
 * every screen looks the same. Change a color here and the whole app changes.
 */
public class Theme {
    public static final Color WARM_CREAM = new Color(248, 246, 242);   // window background
    public static final Color SOFT_CARD = new Color(252, 250, 248);    // cards in the workspace
    public static final Color WARM_BEIGE = new Color(230, 225, 218);   // secondary buttons, borders
    public static final Color DEEP_MOCHA = new Color(92, 64, 51);      // main brown (titles, main buttons)
    public static final Color TEXT_DARK = new Color(60, 50, 45);
    public static final Color CARD_BORDER = new Color(220, 215, 210);
    public static final Color SUBTLE_TEXT = new Color(120, 110, 105);
    public static final Color LOGOUT_BUTTON = new Color(210, 180, 170);
    public static final Color NEW_JOURNAL_BUTTON = new Color(248, 240, 227);

    /**
     * The original dashboard used the font name "TimesNewRoman". That is not a real
     * font name (the real one is "Times New Roman"), so Java quietly used its default
     * font instead. I kept the exact same text so the dashboard looks exactly as before.
     */
    public static final String DASHBOARD_FONT = "TimesNewRoman";

    /** Brown button with white text (the main action on a screen). */
    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(DEEP_MOCHA);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    /** Beige button with dark text (secondary actions). */
    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(WARM_BEIGE);
        button.setForeground(TEXT_DARK);
        button.setFocusPainted(false);
        return button;
    }

    /**
     * Shows your logo if the file resources/logo.png exists, otherwise returns null.
     * To use your logo: copy it to the DAPAS folder as resources\logo.png.
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
     * (A plain DefaultTableModel lets the user type over the numbers.)
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
