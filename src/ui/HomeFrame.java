package ui;

import model.Formats;
import model.JournalEntry;
import model.User;
import service.AppException;
import service.JournalService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * The dashboard you see after logging in (was "homepage" in the old project).
 *
 * The old version listed text FILES. Now everything lives in one database, so the
 * two lists show journal ENTRIES instead: the 5 newest, and all of them.
 * Double-click an entry (or press New Journal) to open the journal screen.
 */
public class HomeFrame extends JFrame {

    private final User user;
    private final JournalService journalService = new JournalService();
    private final DefaultListModel<String> recentListModel = new DefaultListModel<>();
    private final DefaultListModel<String> allListModel = new DefaultListModel<>();

    public HomeFrame(User user) {
        this.user = user;
        setTitle("Accounting System - Dashboard (" + user.getUsername() + ")");
        setSize(850, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        buildScreen();
        loadLists();
    }

    private void buildScreen() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Theme.WARM_CREAM);

        // Top bar: welcome text and Log Out
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUsername() + "!");
        welcomeLabel.setFont(new Font(Theme.DASHBOARD_FONT, Font.BOLD, 20));
        welcomeLabel.setForeground(Theme.DEEP_MOCHA);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setBackground(Theme.LOGOUT_BUTTON);
        logoutButton.setForeground(Theme.TEXT_DARK);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            dispose();
            new WelcomeFrame().setVisible(true);
        });

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.WARM_CREAM);
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(logoutButton, BorderLayout.EAST);

        // Center: top-left recent list, top-right New Journal button, bottom full list
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Theme.WARM_CREAM);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;

        JPanel recentBox = makeListBox("Recent Journal Entries", recentListModel);

        JPanel newJournalBox = new JPanel(new BorderLayout());
        newJournalBox.setBackground(Theme.SOFT_CARD);
        newJournalBox.setBorder(cardBorder());
        JButton newJournalButton = new JButton("<html><center><font size='+3'>+</font><br>New Journal</center></html>");
        newJournalButton.setBackground(Theme.NEW_JOURNAL_BUTTON);
        newJournalButton.setForeground(Color.BLACK);
        newJournalButton.setFocusPainted(false);
        newJournalButton.setFont(new Font(Theme.DASHBOARD_FONT, Font.BOLD, 14));
        newJournalButton.addActionListener(e -> openJournal());
        newJournalBox.add(newJournalButton, BorderLayout.CENTER);

        JPanel allBox = makeListBox("All Journal Entries", allListModel);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.65; gbc.weighty = 0.5;
        centerPanel.add(recentBox, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35; gbc.weighty = 0.5;
        centerPanel.add(newJournalBox, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.5;
        centerPanel.add(allBox, gbc);

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    /** A white card with a heading and a list. Double-clicking an item opens the journal. */
    private JPanel makeListBox(String heading, DefaultListModel<String> model) {
        JPanel box = new JPanel(new BorderLayout(8, 8));
        box.setBackground(Theme.SOFT_CARD);
        box.setBorder(cardBorder());

        JLabel headingLabel = new JLabel(heading);
        headingLabel.setFont(new Font(Theme.DASHBOARD_FONT, Font.BOLD, 13));
        headingLabel.setForeground(Theme.DEEP_MOCHA);

        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new Font(Theme.DASHBOARD_FONT, Font.PLAIN, 13));
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && list.getSelectedValue() != null) {
                    openJournal();
                }
            }
        });

        box.add(headingLabel, BorderLayout.NORTH);
        box.add(new JScrollPane(list), BorderLayout.CENTER);
        return box;
    }

    private javax.swing.border.Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.WARM_BEIGE, 1),
                new EmptyBorder(10, 12, 10, 12));
    }

    private void loadLists() {
        recentListModel.clear();
        allListModel.clear();
        try {
            for (JournalEntry entry : journalService.getRecentEntries(5)) {
                recentListModel.addElement(describe(entry));
            }
            for (JournalEntry entry : journalService.getAllEntries()) {
                allListModel.addElement(describe(entry));
            }
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** One line of text for a list: "#3   27-10-2026   Owner invested cash   [DRAFT]" */
    private String describe(JournalEntry entry) {
        return "#" + entry.getId() + "   " + Formats.isoToDisplayDate(entry.getEntryDate())
                + "   " + entry.getDescription() + "   [" + entry.getStatus() + "]";
    }

    private void openJournal() {
        dispose();
        new JournalFrame(user).setVisible(true);
    }
}
