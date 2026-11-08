package home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class journal extends JPanel {

    public static class EntryLine {
        public String account, type;
        public double debit, credit;

        public EntryLine(String account, String type, double debit, double credit) {
            this.account = account;
            this.type = type;
            this.debit = debit;
            this.credit = credit;
        }
    }

    public static class JournalRecord {
        public int id;
        public String date, description;
        public List<EntryLine> lines;
        public boolean isVoided;
        public String voidReason;
        public boolean isAdjusting;

        public JournalRecord(int id, String date, String description, List<EntryLine> lines) {
            this(id, date, description, lines, false, "", false);
        }

        public JournalRecord(int id, String date, String description, List<EntryLine> lines, boolean isVoided, String voidReason) {
            this(id, date, description, lines, isVoided, voidReason, false);
        }

        public JournalRecord(int id, String date, String description, List<EntryLine> lines, boolean isVoided, String voidReason, boolean isAdjusting) {
            this.id = id;
            this.date = date;
            this.description = description;
            this.lines = lines;
            this.isVoided = isVoided;
            this.voidReason = voidReason;
            this.isAdjusting = isAdjusting;
        }
    }

    public List<JournalRecord> allEntries = new ArrayList<>();

    private int counter = 1;
    private String currentJournalFile;
    private String username;
    private DefaultTableModel tableModel;
    private JTable table;
    private DatePicker datePicker;

    private final String[] ACCOUNT_CHOICES = {
        "CASH", "ACCOUNTS RECEIVABLE", "SUPPLIES", "PREPAID INSURANCE", "PREPAID RENT",
        "EQUIPMENT", "ACCUMULATED DEPRECIATION - EQUIPMENT", "FURNITURE", "VEHICLES",
        "ACCOUNTS PAYABLE", "UNEARNED REVENUE", "NOTES PAYABLE", "SALARIES PAYABLE",
        "OWNER'S CAPITAL", "OWNER'S DRAWING", "SERVICE REVENUE", "EXPENSES",
        "ADVERTISING EXPENSE", "UTILITIES EXPENSE", "SALARIES EXPENSE",
        "MAINTENANCE EXPENSE", "PROFESSIONAL FEES EXPENSE", "REPAIRS EXPENSE",
        "RENT EXPENSE", "INTEREST EXPENSE", "INSURANCE EXPENSE", "SUPPLIES EXPENSE",
        "DEPRECIATION EXPENSE ACCOUNTS"
    };

    // UI Colors matching Image #9
    private final Color SIDEBAR_BG = new Color(20, 20, 20);
    private final Color MAIN_BG = new Color(245, 245, 245);
    private final Color CARD_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(220, 220, 220);
    private final Color TEXT_DARK = new Color(40, 40, 40);
    private final Color TEXT_MUTED = new Color(120, 120, 120);
    private final Color BUTTON_BLACK = new Color(10, 10, 10);

    public journal(String fileName) {
        this(fileName, "admin");
    }

    public journal(String fileName, String username) {
        this.currentJournalFile = fileName;
        this.username = username;
        loadJournalsFromFile();

        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        // Sidebar Navigation (Left)
        add(createSidebar(), BorderLayout.WEST);

        // Main Content Scrollable Area
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(MAIN_BG);
        mainContent.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Top Header ("Journal entry" + Refresh button)
        mainContent.add(createTopHeader(), BorderLayout.NORTH);

        // Center Panel holding Form + Table
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(MAIN_BG);

        centerPanel.add(createFormCard());
        centerPanel.add(Box.createVerticalStrut(25));
        centerPanel.add(createTableCard());

        JScrollPane mainScroll = new JScrollPane(centerPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        mainScroll.getViewport().setBackground(MAIN_BG);

        mainContent.add(mainScroll, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
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

        navLinks.add(createNavLink("Dashboard", false));
        navLinks.add(Box.createVerticalStrut(10));
        navLinks.add(createNavLink("Journal entry", true));
        navLinks.add(Box.createVerticalStrut(10));
        navLinks.add(createNavLink("Ledger", false));
        navLinks.add(Box.createVerticalStrut(10));
        navLinks.add(createNavLink("12-column sheet", false));

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
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton logoutBtn = new JButton("Log out");
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setMaximumSize(new Dimension(180, 40));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            new ui.LoginFrame().setVisible(true);
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
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
            if (text.equals("Dashboard")) {
                new homepage(username).showDashboard();
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) w.dispose();
            } else if (text.equals("Ledger")) {
                switchView(new ledger(this, username));
            } else if (text.equals("12-column sheet")) {
                switchView(new columnar(this, username));
            }
        });

        return btn;
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAIN_BG);

        JLabel title = new JLabel("Journal entry");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(Color.BLACK);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setForeground(Color.BLACK);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                new EmptyBorder(8, 20, 8, 20)));
        refreshBtn.addActionListener(e -> {
            loadJournalsFromFile();
            refreshTable();
        });

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel createFormCard() {
        JPanel card = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
        card.setLayout(new BorderLayout(15, 15));
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // Date
        datePicker = new DatePicker();
        // Set initial date
        datePicker.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

        // Category combo
        String[] entryOptions = {"Regular Entry", "Adjusting Entry"};
        JComboBox<String> entryTypeCombo = new JComboBox<>(entryOptions);
        styleComboBox(entryTypeCombo);

        // Description Text Area
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        descScroll.setBackground(Color.WHITE);

        // Account fields & combos
        JComboBox<String> drAccountCombo = new JComboBox<>(ACCOUNT_CHOICES);
        JComboBox<String> crAccountCombo = new JComboBox<>(ACCOUNT_CHOICES);
        styleComboBox(drAccountCombo);
        styleComboBox(crAccountCombo);
        JTextField amtField = createStyledTextField("");

        String[] accountTypes = {"ASSET", "EQUITY", "EXPENSE", "LIABILITY", "REVENUE"};
        JComboBox<String> drTypeCombo = new JComboBox<>(accountTypes);
        styleComboBox(drTypeCombo);
        JComboBox<String> crTypeCombo = new JComboBox<>(accountTypes);
        styleComboBox(crTypeCombo);

        // ROW 0: Date (col 0), Entry Category (col 1), Description (col 2, span 3 rows)
        gbc.gridy = 0;
        gbc.gridx = 0; gbc.weightx = 0.25; gbc.gridheight = 1;
        formGrid.add(createFieldGroup("Date", datePicker), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        formGrid.add(createFieldGroup("Entry category", entryTypeCombo), gbc);

        gbc.gridx = 2; gbc.weightx = 0.4; gbc.gridheight = 4;
        formGrid.add(createFieldGroup("Description", descScroll), gbc);

        // ROW 1: Debit account (col 0), Type (col 1)
        gbc.gridy = 1; gbc.gridheight = 1;
        gbc.gridx = 0; gbc.weightx = 0.25;
        formGrid.add(createFieldGroup("Debit account", drAccountCombo), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        formGrid.add(createFieldGroup("Type", drTypeCombo), gbc);

        // ROW 2: Credit account (col 0), Type (col 1)
        gbc.gridy = 2;
        gbc.gridx = 0; gbc.weightx = 0.25;
        formGrid.add(createFieldGroup("Credit account", crAccountCombo), gbc);

        gbc.gridx = 1; gbc.weightx = 0.35;
        formGrid.add(createFieldGroup("Type", crTypeCombo), gbc);

        // ROW 3: Amount (col 0, span 2)
        gbc.gridy = 3;
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 0.6;
        formGrid.add(createFieldGroup("Amount", amtField), gbc);

        // BUTTON BAR
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnBar.setBackground(CARD_BG);
        btnBar.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton clearBtn = createStyledButton("Clear form", false);
        JButton addBtn = createStyledButton("Add transaction", true);
        JButton editBtn = createStyledButton("Edit selected", true);
        JButton voidBtn = createStyledButton("Void selected", false);

        btnBar.add(clearBtn);
        btnBar.add(addBtn);
        btnBar.add(editBtn);
        btnBar.add(voidBtn);

        card.add(formGrid, BorderLayout.CENTER);
        card.add(btnBar, BorderLayout.SOUTH);

        // Actions
        final JournalRecord[] editingRecord = {null};

        clearBtn.addActionListener(e -> {
            descArea.setText("");
            amtField.setText("");
            drAccountCombo.setSelectedIndex(0);
            crAccountCombo.setSelectedIndex(0);
            datePicker.setDate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            entryTypeCombo.setSelectedIndex(0);
            entryTypeCombo.setEnabled(true);
            drTypeCombo.setSelectedIndex(0);
            crTypeCombo.setSelectedIndex(0);
            editingRecord[0] = null;
            addBtn.setText("Add transaction");
        });

        addBtn.addActionListener(e -> {
            try {
                String date = datePicker.getDateString();
                String desc = descArea.getText().trim();
                String drAcc = (String) drAccountCombo.getSelectedItem();
                String crAcc = (String) crAccountCombo.getSelectedItem();
                String drType = (String) drTypeCombo.getSelectedItem();
                String crType = (String) crTypeCombo.getSelectedItem();
                String amtText = amtField.getText().trim();

                if (date.isEmpty() || desc.isEmpty() || drAcc.isEmpty() || crAcc.isEmpty() || amtText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (drAcc.equalsIgnoreCase(crAcc)) {
                    JOptionPane.showMessageDialog(this, "Debit and Credit accounts must be different.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double amount = Double.parseDouble(amtText);
                if (amount <= 0 || Double.isInfinite(amount) || Double.isNaN(amount)) {
                    JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                List<EntryLine> updatedLines = new ArrayList<>();
                updatedLines.add(new EntryLine(drAcc, drType, amount, 0.0));
                updatedLines.add(new EntryLine(crAcc, crType, 0.0, amount));

                if (editingRecord[0] != null) {
                    JournalRecord record = editingRecord[0];
                    record.date = date;
                    record.description = desc;
                    record.lines = updatedLines;
                    saveChangesFromColumnar();
                    JOptionPane.showMessageDialog(this, "Entry ID " + record.id + " updated successfully!");
                    editingRecord[0] = null;
                    addBtn.setText("Add transaction");
                    entryTypeCombo.setEnabled(true);
                } else {
                    boolean isAdjusting = "Adjusting Entry".equals(entryTypeCombo.getSelectedItem());
                    JournalRecord record = new JournalRecord(counter++, date, desc, updatedLines, false, "", isAdjusting);
                    allEntries.add(record);
                    saveChangesFromColumnar();
                    JOptionPane.showMessageDialog(this, "Transaction Confirmed!");
                }

                refreshTable();
                clearBtn.doClick();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an entry to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(selectedRow);
            int selectedId = (int) tableModel.getValueAt(modelRow, 0);
            JournalRecord record = findRecordById(selectedId);
            if (record == null) return;

            if (record.isVoided) {
                JOptionPane.showMessageDialog(this, "A voided entry cannot be edited.", "Voided Entry", JOptionPane.WARNING_MESSAGE);
                return;
            }

            EntryLine debitLine = null;
            EntryLine creditLine = null;
            for (EntryLine line : record.lines) {
                if (line.debit > 0) debitLine = line;
                if (line.credit > 0) creditLine = line;
            }
            if (debitLine == null || creditLine == null) return;

            editingRecord[0] = record;
            datePicker.setDate(record.date);
            descArea.setText(record.description);
            drAccountCombo.setSelectedItem(debitLine.account);
            crAccountCombo.setSelectedItem(creditLine.account);
            drTypeCombo.setSelectedItem(debitLine.type);
            crTypeCombo.setSelectedItem(creditLine.type);
            amtField.setText(String.valueOf(debitLine.debit));
            entryTypeCombo.setSelectedItem(record.isAdjusting ? "Adjusting Entry" : "Regular Entry");
            addBtn.setText("Save changes");
        });

        voidBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a journal entry to void.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = table.convertRowIndexToModel(selectedRow);
            int selectedId = (int) tableModel.getValueAt(modelRow, 0);
            JournalRecord selectedRecord = findRecordById(selectedId);
            if (selectedRecord == null) return;

            if (selectedRecord.isVoided) {
                JOptionPane.showMessageDialog(this, "This transaction has already been voided.", "Already Voided", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String reason = JOptionPane.showInputDialog(this, "Reason for voiding ID " + selectedRecord.id + ":");
            if (reason != null && !reason.trim().isEmpty()) {
                selectedRecord.isVoided = true;
                selectedRecord.voidReason = reason.trim();
                saveChangesFromColumnar();
                refreshTable();
                JOptionPane.showMessageDialog(this, "Entry ID " + selectedRecord.id + " voided.");
            }
        });

        return card;
    }

    private JPanel createFieldGroup(String labelStr, Component comp) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(CARD_BG);
        JLabel label = new JLabel(labelStr);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_DARK);
        panel.add(label, BorderLayout.NORTH);
        panel.add(comp, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)));
        return field;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    }

    private JButton createStyledButton(String text, boolean dark) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (dark) {
            btn.setBackground(BUTTON_BLACK);
            btn.setForeground(Color.WHITE);
            btn.setBorder(new EmptyBorder(12, 22, 12, 22));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_DARK);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1),
                    new EmptyBorder(12, 22, 12, 22)));
        }
        return btn;
    }

    private JPanel createTableCard() {
        JPanel card = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        String[] cols = {"ID", "Date", "Description", "Category", "Account", "Type", "Debit", "Credit", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(42);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        header.setForeground(TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setOpaque(true);
                lbl.setBackground(s ? t.getSelectionBackground() : Color.WHITE);
                lbl.setForeground(TEXT_DARK);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(230, 230, 230)),
                        new EmptyBorder(8, 12, 8, 12)
                ));
                return lbl;
            }
        });

        // Status column badge renderer matching reference Image #9
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
                p.setBackground(s ? t.getSelectionBackground() : Color.WHITE);
                p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
                JLabel lbl = new JLabel(v != null ? v.toString() : "") {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.BLACK);
                        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                lbl.setBorder(new EmptyBorder(3, 12, 3, 12));
                p.add(lbl);
                return p;
            }
        });

        populateExistingTableRows();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(0, 300));

        JPanel tableWrapper = new RoundedPanel(12, Color.WHITE, BORDER_COLOR);
        tableWrapper.setLayout(new BorderLayout());
        tableWrapper.setBorder(new EmptyBorder(2, 2, 2, 2));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        card.add(tableWrapper, BorderLayout.CENTER);

        return card;
    }

    public JournalRecord findRecordById(int id) {
        for (JournalRecord record : allEntries) {
            if (record.id == id) return record;
        }
        return null;
    }

    public String getCurrentJournalFile() {
        return currentJournalFile;
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        populateExistingTableRows();
    }

    private void populateExistingTableRows() {
        for (JournalRecord record : allEntries) {
            String status = record.isVoided ? "VOIDED" : "ACTIVE";
            String entryCategory = record.isAdjusting ? "Adjusting Entry" : "Regular Entry";

            for (EntryLine line : record.lines) {
                tableModel.addRow(new Object[]{
                        record.id,
                        record.date,
                        record.description,
                        entryCategory,
                        line.account,
                        line.type,
                        String.format("%.2f", line.debit),
                        String.format("%.2f", line.credit),
                        status
                });
            }
        }
    }

    public void loadJournalsFromFile() {
        File file = new File(currentJournalFile);
        if (!file.exists()) {
            file = new File("../" + currentJournalFile);
        }
        if (!file.exists()) return;

        currentJournalFile = file.getPath();
        allEntries.clear();

        try (Scanner scanner = new Scanner(file)) {
            int maxId = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[0]);
                    maxId = Math.max(maxId, id);
                    String date = parts[1];
                    String desc = parts[2];

                    List<EntryLine> lines = new ArrayList<>();
                    String[] lineItems = parts[3].split(";");
                    for (String item : lineItems) {
                        String[] fields = item.split(",", -1);
                        if (fields.length >= 4) {
                            lines.add(new EntryLine(
                                    fields[0], fields[1],
                                    Double.parseDouble(fields[2]),
                                    Double.parseDouble(fields[3])
                            ));
                        }
                    }

                    boolean isVoided = parts.length >= 5 && Boolean.parseBoolean(parts[4]);
                    String voidReason = parts.length >= 6 ? parts[5] : "";
                    boolean isAdjusting = parts.length >= 7 && Boolean.parseBoolean(parts[6]);

                    allEntries.add(new JournalRecord(id, date, desc, lines, isVoided, voidReason, isAdjusting));
                }
            }
            counter = maxId + 1;
        } catch (Exception e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }

    public void saveChangesFromColumnar() {
        saveJournalsToFile();
    }

    private void saveJournalsToFile() {
        try (FileWriter writer = new FileWriter(currentJournalFile, false)) {
            for (JournalRecord rec : allEntries) {
                StringBuilder sb = new StringBuilder();
                sb.append(rec.id).append("|")
                        .append(rec.date).append("|")
                        .append(rec.description).append("|");

                for (int i = 0; i < rec.lines.size(); i++) {
                    EntryLine line = rec.lines.get(i);
                    sb.append(line.account).append(",")
                            .append(line.type).append(",")
                            .append(line.debit).append(",")
                            .append(line.credit);
                    if (i < rec.lines.size() - 1) sb.append(";");
                }

                sb.append("|").append(rec.isVoided)
                        .append("|").append(rec.voidReason == null ? "" : rec.voidReason)
                        .append("|").append(rec.isAdjusting);

                writer.write(sb.toString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public void switchView(JPanel panel) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.getContentPane().removeAll();
            frame.getContentPane().add(panel);
            frame.revalidate();
            frame.repaint();
        }
    }

    public static class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;
        private Color borderColor;

        public RoundedPanel(int radius, Color bgColor, Color borderColor) {
            super();
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground() != null && !getBackground().equals(bgColor) ? getBackground() : bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
        }
    }
}
