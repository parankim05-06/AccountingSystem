package ui;

import model.User;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * The three tabs at the top of every workspace screen:
 *   JOURNAL ENTRY | LEDGER | 12 COLUMNAR SHEET
 * The tab for the screen you are on is brown. Clicking another tab closes the
 * current window and opens that screen.
 */
public class NavBar extends JPanel {

    public static final int JOURNAL = 0;
    public static final int LEDGER = 1;
    public static final int COLUMNAR = 2;

    private static final String[] TAB_NAMES = {"JOURNAL ENTRY", "LEDGER", "12 COLUMNAR SHEET"};

    public NavBar(JFrame currentFrame, User user, int activeTab) {
        super(new GridLayout(1, 3, 10, 0));
        setBackground(Theme.WARM_CREAM);
        setPreferredSize(new Dimension(0, 40));

        for (int tab = 0; tab < TAB_NAMES.length; tab++) {
            JButton button = new JButton(TAB_NAMES[tab]);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
            button.setFocusPainted(false);

            if (tab == activeTab) {
                button.setBackground(Theme.DEEP_MOCHA);
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(Theme.WARM_BEIGE);
                button.setForeground(Theme.TEXT_DARK);
                final int tabToOpen = tab;
                button.addActionListener(e -> {
                    currentFrame.dispose();
                    openTab(tabToOpen, user);
                });
            }
            add(button);
        }
    }

    private void openTab(int tab, User user) {
        JFrame next;
        if (tab == JOURNAL) {
            next = new JournalFrame(user);
        } else if (tab == LEDGER) {
            next = new LedgerFrame(user);
        } else {
            next = new ColumnarFrame(user);
        }
        next.setVisible(true);
    }
}
