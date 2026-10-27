package home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class homepage extends JFrame {
    private String username;
    private DefaultListModel<String> recentListModel;
    private JList<String> recentList;
    private DefaultListModel<String> savedListModel;
    private JList<String> savedList;

    private final Color WARM_CREAM = new Color(248, 246, 242);
    private final Color SOFT_CARD = new Color(252, 250, 248);
    private final Color WARM_BEIGE = new Color(230, 225, 218);
    private final Color DEEP_MOCHA = new Color(92, 64, 51);
    private final Color TEXT_DARK = new Color(60, 50, 45);

    public homepage(String username) {
        this.username = username;
        setTitle("Accounting System - Dashboard (" + username + ")");
        setSize(850, 620);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    public void showDashboard() {
        setVisible(true);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(WARM_CREAM);

        // Top Welcome Header
        JLabel welcomeLbl = new JLabel("Welcome, " + username + "!");
        welcomeLbl.setFont(new Font("TimesNewRoman", Font.BOLD, 20));
        welcomeLbl.setForeground(DEEP_MOCHA);

        JButton logoutBtn = new JButton("Log Out");
        logoutBtn.setBackground(new Color(210, 180, 170));
        logoutBtn.setForeground(TEXT_DARK);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> dispose());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(WARM_CREAM);
        topBar.add(welcomeLbl, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        // Center Layout Matching Sketch: Top-Left (Recent), Top-Right (New Journal), Bottom (Saved Files)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(WARM_CREAM);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;

        // 1. Recently Opened Files Box (Top-Left)
        JPanel recentBox = new JPanel(new BorderLayout(8, 8));
        recentBox.setBackground(SOFT_CARD);
        recentBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARM_BEIGE, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel recentHeader = new JLabel("Recently Opened Files");
        recentHeader.setFont(new Font("TimesNewRoman", Font.BOLD, 13));
        recentHeader.setForeground(DEEP_MOCHA);

        recentListModel = new DefaultListModel<>();
        recentList = new JList<>(recentListModel);
        recentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentList.setFont(new Font("TimesNewRoman", Font.PLAIN, 13));
        
        // Double-click to open file
        recentList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selected = recentList.getSelectedValue();
                    if (selected != null) openJournalFile(selected);
                }
            }
        });

        JScrollPane recentScroll = new JScrollPane(recentList);
        recentBox.add(recentHeader, BorderLayout.NORTH);
        recentBox.add(recentScroll, BorderLayout.CENTER);

        // 2. New Journal Box / Button (Top-Right)
        JPanel newJournalBox = new JPanel(new BorderLayout());
        newJournalBox.setBackground(SOFT_CARD);
        newJournalBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARM_BEIGE, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JButton newJournalBtn = new JButton("<html><center><font size='+3'>+</font><br>New Journal</center></html>");
        newJournalBtn.setBackground(new Color(248, 240, 227));
        newJournalBtn.setForeground(Color.BLACK);
        newJournalBtn.setFocusPainted(false);
        newJournalBtn.setFont(new Font("TimesNewRoman", Font.BOLD, 14));
        newJournalBtn.addActionListener(e -> createNewJournal());

        newJournalBox.add(newJournalBtn, BorderLayout.CENTER);

        // 3. Saved Files Box (Bottom - Full Width)
        JPanel savedBox = new JPanel(new BorderLayout(8, 8));
        savedBox.setBackground(SOFT_CARD);
        savedBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARM_BEIGE, 1),
            new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel savedHeader = new JLabel("Saved Files");
        savedHeader.setFont(new Font("TimesNewRoman", Font.BOLD, 13));
        savedHeader.setForeground(DEEP_MOCHA);

        savedListModel = new DefaultListModel<>();
        savedList = new JList<>(savedListModel);
        savedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        savedList.setFont(new Font("TimesNewRoman", Font.PLAIN, 13));

        // Double-click to open file
        savedList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selected = savedList.getSelectedValue();
                    if (selected != null) openJournalFile(selected);
                }
            }
        });

        JScrollPane savedScroll = new JScrollPane(savedList);
        savedBox.add(savedHeader, BorderLayout.NORTH);
        savedBox.add(savedScroll, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.65; gbc.weighty = 0.5;
        centerPanel.add(recentBox, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.35; gbc.weighty = 0.5;
        centerPanel.add(newJournalBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.weighty = 0.5;
        centerPanel.add(savedBox, gbc);

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
        loadFiles();
    }

    private void createNewJournal() {
        String journalName = JOptionPane.showInputDialog(this, "Enter Journal Name:");
        if (journalName != null && !journalName.trim().isEmpty()) {
            String cleanName = journalName.trim();
            String fileName = "journal_" + username + "_" + cleanName + ".txt";
            try {
                File f = new File(fileName);
                f.createNewFile();
                f.setLastModified(System.currentTimeMillis());
                
                // Open newly created journal directly
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

        if (userFiles != null && userFiles.length > 0) {
            File[] recentFiles = userFiles.clone();
            Arrays.sort(recentFiles, Comparator.comparingLong(File::lastModified).reversed());

            File[] savedFiles = userFiles.clone();
            Arrays.sort(savedFiles, Comparator.comparing(File::getName));

            int recentLimit = Math.min(recentFiles.length, 5);
            for (int i = 0; i < recentLimit; i++) {
                String displayName = recentFiles[i].getName()
                    .replace("journal_" + username + "_", "")
                    .replace(".txt", "");
                recentListModel.addElement(displayName);
            }

            for (File file : savedFiles) {
                String displayName = file.getName()
                    .replace("journal_" + username + "_", "")
                    .replace(".txt", "");
                savedListModel.addElement(displayName);
            }
        } else {
            String defaultName = "journal_" + username + "_default.txt";
            try {
                File f = new File(defaultName);
                f.createNewFile();
                f.setLastModified(System.currentTimeMillis());
                recentListModel.addElement("default");
                savedListModel.addElement("default");
            } catch (Exception ignored) {}
        }
    }

    private void openJournalFile(String fileNameOnly) {
        File fileToTouch = new File("journal_" + username + "_" + fileNameOnly + ".txt");
        fileToTouch.setLastModified(System.currentTimeMillis());

        dispose();
        JFrame frame = new JFrame("Accounting System Workspace");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.add(new journal(fileToTouch.getName(), username));
        frame.setVisible(true);
    }
}