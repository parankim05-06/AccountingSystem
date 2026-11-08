package home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class homepage extends JFrame {
    private String username;
    private DefaultListModel<String> recentListModel;
    private DefaultListModel<String> savedListModel;
    private JPanel recentListPanel;
    private JPanel savedListPanel;

    // UI Colors matching the reference image
    private final Color SIDEBAR_BG = new Color(20, 20, 20);
    private final Color MAIN_BG = new Color(245, 245, 245);
    private final Color CARD_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(220, 220, 220);
    private final Color TEXT_DARK = new Color(40, 40, 40);

    public homepage(String username) {
        this.username = username;
        setTitle("Accounting System - Dashboard");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        recentListModel = new DefaultListModel<>();
        savedListModel = new DefaultListModel<>();

        initUI();
        loadFiles();
        // Wait briefly for UI components to be created
        // then reload files to populate the panels
        SwingUtilities.invokeLater(this::reloadFiles);
    }

    public void reloadFiles() {
        loadFiles();
    }

    public void showDashboard() {
        setVisible(true);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(MAIN_BG);

        // Sidebar
        root.add(createSidebar(), BorderLayout.WEST);

        // Main Content Area
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(MAIN_BG);
        mainContent.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Welcome Header
        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.BLACK);
        mainContent.add(welcomeLabel, BorderLayout.NORTH);

        // Center Container for Cards
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(MAIN_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 25, 25);
        gbc.fill = GridBagConstraints.BOTH;

        // Top Row: Recently Opened Files (Left)
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.6; gbc.weighty = 0.4;
        centerContainer.add(createRecentCard(), gbc);

        // Top Row: New Journal (Right)
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.4; gbc.weighty = 0.4;
        gbc.insets = new Insets(0, 0, 25, 0);
        centerContainer.add(createNewJournalCard(), gbc);

        // Bottom Row: Saved Files (Full Width)
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0; gbc.weighty = 0.6;
        gbc.insets = new Insets(0, 0, 0, 0);
        centerContainer.add(createSavedCard(), gbc);

        mainContent.add(centerContainer, BorderLayout.CENTER);
        root.add(mainContent, BorderLayout.CENTER);

        add(root);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Logo
        JLabel logo = new JLabel("📋 ENTRI");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logo.setBorder(new EmptyBorder(0, 0, 40, 0));

        // Navigation Links
        JPanel navLinks = new JPanel();
        navLinks.setLayout(new BoxLayout(navLinks, BoxLayout.Y_AXIS));
        navLinks.setBackground(SIDEBAR_BG);

        navLinks.add(createNavLink("Dashboard", true));

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(SIDEBAR_BG);
        topSection.add(logo, BorderLayout.NORTH);
        topSection.add(navLinks, BorderLayout.CENTER);

        // Bottom Section
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setBackground(SIDEBAR_BG);

        JLabel userLabel = new JLabel("Signed in as " + username);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userLabel.setForeground(new Color(180, 180, 180));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton logoutBtn = new JButton("Log out");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setMaximumSize(new Dimension(180, 40));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            new ui.LoginFrame().setVisible(true);
            dispose();
        });

        bottomSection.add(userLabel);
        bottomSection.add(Box.createVerticalStrut(10));
        bottomSection.add(logoutBtn);

        sidebar.add(topSection, BorderLayout.NORTH);
        sidebar.add(bottomSection, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavLink(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(0, 15, 0, 15));

        if (active) {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
        } else {
            btn.setBackground(SIDEBAR_BG);
            btn.setForeground(new Color(180, 180, 180));
            btn.setBorder(null);
        }

        btn.addActionListener(e -> {
            if (text.equals("Journal entry")) {
                new ui.JournalFrame(new model.User(0, username, "", "")).setVisible(true);
                dispose();
            }
            // Add other navigation as needed
        });

        return btn;
    }

    private JPanel createRecentCard() {
        JPanel card = createBaseCard("Recently opened files");
        recentListPanel = new JPanel();
        recentListPanel.setLayout(new BoxLayout(recentListPanel, BoxLayout.Y_AXIS));
        recentListPanel.setBackground(CARD_BG);

        // We'll update this in loadFiles()
        card.add(new JScrollPane(recentListPanel), BorderLayout.CENTER);
        return card;
    }

    private JPanel createSavedCard() {
        JPanel card = createBaseCard("Saved files");
        savedListPanel = new JPanel();
        savedListPanel.setLayout(new BoxLayout(savedListPanel, BoxLayout.Y_AXIS));
        savedListPanel.setBackground(CARD_BG);

        // We'll update this in loadFiles()
        card.add(new JScrollPane(savedListPanel), BorderLayout.CENTER);
        return card;
    }

    private JPanel createBaseCard(String title) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(25, 25, 25, 25)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);
        card.add(titleLabel, BorderLayout.NORTH);

        return card;
    }

    private JPanel createNewJournalCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(15, 15, 15)); // Black card
        card.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(15, 15, 15));

        JLabel plusIcon = new JLabel("+");
        plusIcon.setFont(new Font("Segoe UI", Font.BOLD, 48));
        plusIcon.setForeground(Color.WHITE);
        plusIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel("New journal");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(plusIcon);
        content.add(Box.createVerticalStrut(10));
        content.add(label);

        card.add(content);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                createNewJournal();
            }
        });

        return card;
    }

    private void createNewJournal() {
        String journalName = JOptionPane.showInputDialog(this, "Enter Journal Name:");
        if (journalName != null && !journalName.trim().isEmpty()) {
            String cleanName = journalName.trim();
            String fileName = "journal_" + username + "_" + cleanName + ".txt";
            try {
                File f = new File(fileName);
                if (f.createNewFile()) {
                    f.setLastModified(System.currentTimeMillis());
                }
                openJournalFile(cleanName);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error creating file.");
            }
        }
    }

    private void loadFiles() {
        recentListModel.clear();
        savedListModel.clear();

        File folder = new File(".");
        File[] userFiles = folder.listFiles((dir, name) ->
                name.startsWith("journal_" + username + "_") && name.endsWith(".txt")
        );

        if (recentListPanel == null || savedListPanel == null) return;

        recentListPanel.removeAll();
        savedListPanel.removeAll();

        if (userFiles != null && userFiles.length > 0) {
            // Recent files (sorted by date)
            File[] recentFiles = userFiles.clone();
            Arrays.sort(recentFiles, Comparator.comparingLong(File::lastModified).reversed());
            for (int i = 0; i < Math.min(recentFiles.length, 3); i++) {
                String name = recentFiles[i].getName().replace("journal_" + username + "_", "").replace(".txt", "");
                recentListPanel.add(createFileRow(name));
                recentListPanel.add(Box.createVerticalStrut(15));
            }

            // Saved files (alphabetical)
            File[] savedFiles = userFiles.clone();
            Arrays.sort(savedFiles, Comparator.comparing(File::getName));
            for (File file : savedFiles) {
                String name = file.getName().replace("journal_" + username + "_", "").replace(".txt", "");
                savedListPanel.add(createFileRow(name));
                savedListPanel.add(Box.createVerticalStrut(15));
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createFileRow(String name) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CARD_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        nameLabel.setForeground(TEXT_DARK);

        JButton openBtn = new JButton("Open");
        openBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        openBtn.setForeground(TEXT_DARK);
        openBtn.setBackground(CARD_BG);
        openBtn.setBorder(null);
        openBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        openBtn.addActionListener(e -> openJournalFile(name));

        row.add(nameLabel, BorderLayout.WEST);
        row.add(openBtn, BorderLayout.EAST);
        row.add(Box.createVerticalGlue(), BorderLayout.SOUTH);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));

        return row;
    }

    private void openJournalFile(String fileNameOnly) {
        String clean = fileNameOnly.trim();
        String fullFileName = "journal_" + username + "_" + clean + ".txt";
        File file = new File(fullFileName);
        file.setLastModified(System.currentTimeMillis());

        new ui.JournalFrame(new model.User(0, username, "", ""), clean).setVisible(true);
        dispose();
    }
}
