package home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
            this.account = account; this.type = type; this.debit = debit; this.credit = credit;
        }
    }

    public static class JournalRecord {
        public int id;
        public String date, description;
        public List<EntryLine> lines;
        public JournalRecord(int id, String date, String description, List<EntryLine> lines) {
            this.id = id; this.date = date; this.description = description; this.lines = lines;
        }
    }

    public List<JournalRecord> allEntries = new ArrayList<>();
    private int counter = 1;
    private String currentJournalFile;
    private String username;
    private DefaultTableModel tableModel;
    private JTextField dateField;

    private final Color WARM_CREAM = new Color(248, 246, 242);
    private final Color SOFT_CARD = new Color(252, 250, 248);
    private final Color WARM_BEIGE = new Color(230, 225, 218);
    private final Color DEEP_MOCHA = new Color(92, 64, 51);
    private final Color TEXT_DARK = new Color(60, 50, 45);

    public journal(String fileName) {
        this(fileName, "admin");
    }

    public journal(String fileName, String username) {
        this.currentJournalFile = fileName;
        this.username = username;
        loadJournalsFromFile();

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(WARM_CREAM);

        // --- TOP NAVIGATION BAR ---
        JPanel navBar = new JPanel(new GridLayout(1, 3, 10, 0));
        navBar.setBackground(WARM_CREAM);
        navBar.setPreferredSize(new Dimension(0, 40));

        JButton journalTab = new JButton("JOURNAL ENTRY");
        journalTab.setBackground(DEEP_MOCHA);
        journalTab.setForeground(Color.WHITE);
        journalTab.setFont(new Font("SansSerif", Font.BOLD, 12));
        journalTab.setFocusPainted(false);

        JButton ledgerTab = new JButton("LEDGER");
        ledgerTab.setBackground(WARM_BEIGE);
        ledgerTab.setForeground(TEXT_DARK);
        ledgerTab.setFont(new Font("SansSerif", Font.BOLD, 12));
        ledgerTab.setFocusPainted(false);
        ledgerTab.addActionListener(e -> {
            loadJournalsFromFile();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
            JFrame frame = new JFrame("Accounting System Workspace");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new ledger(this, username));
            frame.setVisible(true);
        });

        JButton columnarTab = new JButton("12 COLUMNAR SHEET");
        columnarTab.setBackground(WARM_BEIGE);
        columnarTab.setForeground(TEXT_DARK);
        columnarTab.setFont(new Font("SansSerif", Font.BOLD, 12));
        columnarTab.setFocusPainted(false);
        columnarTab.addActionListener(e -> {
            loadJournalsFromFile();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
            JFrame frame = new JFrame("Accounting System Workspace");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new columnar(this, username));
            frame.setVisible(true);
        });

        navBar.add(journalTab);
        navBar.add(ledgerTab);
        navBar.add(columnarTab);

        // --- HEADER WITH REFRESH & DASHBOARD ---
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(WARM_CREAM);

        JLabel moduleTitle = new JLabel("JOURNAL ENTRY");
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        moduleTitle.setForeground(DEEP_MOCHA);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setBackground(WARM_CREAM);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(DEEP_MOCHA);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            loadJournalsFromFile();
            tableModel.setRowCount(0);
            populateExistingTableRows();
            JOptionPane.showMessageDialog(this, "Journal entries refreshed!");
        });

        JButton homeBtn = new JButton("← Dashboard");
        homeBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        homeBtn.setForeground(TEXT_DARK);
        homeBtn.setBackground(WARM_BEIGE);
        homeBtn.setFocusPainted(false);
        homeBtn.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
            new homepage(username).showDashboard();
        });

        headerRight.add(refreshBtn);
        headerRight.add(homeBtn);

        topHeader.add(moduleTitle, BorderLayout.WEST);
        topHeader.add(headerRight, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setBackground(WARM_CREAM);
        topContainer.add(navBar, BorderLayout.NORTH);
        topContainer.add(topHeader, BorderLayout.SOUTH);

        // --- CENTER WORKSPACE CARD ---
        JPanel mainCard = new JPanel(new BorderLayout(15, 15));
        mainCard.setBackground(SOFT_CARD);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARM_BEIGE, 1),
            new EmptyBorder(15, 20, 15, 20)
        ));

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(SOFT_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        dateField = new JTextField(new SimpleDateFormat("dd-MM-yyyy").format(new Date()), 15);

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);

        JTextField drAccountField = new JTextField(15);
        String[] categoryOptions = {"ASSET", "LIABILITY", "EQUITY", "INCOME", "EXPENSE"};
        JComboBox<String> drTypeCombo = new JComboBox<>(categoryOptions);

        JTextField crAccountField = new JTextField(15);
        JComboBox<String> crTypeCombo = new JComboBox<>(categoryOptions);

        JTextField amtField = new JTextField(15);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        formGrid.add(new JLabel("Date (DD-MM-YYYY):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        formGrid.add(dateField, gbc);

        gbc.gridx = 2; gbc.weightx = 0.1;
        formGrid.add(new JLabel("Debit Account & Type:"), gbc);
        JPanel drPanel = new JPanel(new BorderLayout(5, 0));
        drPanel.setBackground(SOFT_CARD);
        drPanel.add(drAccountField, BorderLayout.CENTER);
        drPanel.add(drTypeCombo, BorderLayout.EAST);
        gbc.gridx = 3; gbc.weightx = 0.4;
        formGrid.add(drPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        formGrid.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.gridheight = 2;
        formGrid.add(descScroll, gbc);

        gbc.gridheight = 1;
        gbc.gridx = 2; gbc.weightx = 0.1;
        formGrid.add(new JLabel("Credit Account & Type:"), gbc);
        JPanel crPanel = new JPanel(new BorderLayout(5, 0));
        crPanel.setBackground(SOFT_CARD);
        crPanel.add(crAccountField, BorderLayout.CENTER);
        crPanel.add(crTypeCombo, BorderLayout.EAST);
        gbc.gridx = 3; gbc.weightx = 0.4;
        formGrid.add(crPanel, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.1;
        formGrid.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        formGrid.add(amtField, gbc);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnBar.setBackground(SOFT_CARD);

        JButton clearBtn = new JButton("CLEAR FORM");
        clearBtn.setBackground(Color.WHITE);
        clearBtn.setForeground(TEXT_DARK);
        clearBtn.setFocusPainted(false);

        JButton addBtn = new JButton("ADD TRANSACTION");
        addBtn.setBackground(DEEP_MOCHA);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);

        btnBar.add(clearBtn);
        btnBar.add(addBtn);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(SOFT_CARD);
        formWrapper.add(formGrid, BorderLayout.CENTER);
        formWrapper.add(btnBar, BorderLayout.SOUTH);

        String[] cols = {"ID", "Date", "Description", "Account", "Type", "Debit", "Credit"};
        tableModel = new DefaultTableModel(cols, 0);
        populateExistingTableRows();

        JTable table = new JTable(tableModel);
        table.setRowHeight(22);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(0, 150));

        mainCard.add(formWrapper, BorderLayout.NORTH);
        mainCard.add(tableScroll, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(mainCard, BorderLayout.CENTER);

        clearBtn.addActionListener(e -> {
            descArea.setText("");
            amtField.setText("");
            drAccountField.setText("");
            crAccountField.setText("");
            dateField.setText(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
        });

        addBtn.addActionListener(e -> {
            try {
                String date = dateField.getText().trim();
                String desc = descArea.getText().trim();
                String drAcc = drAccountField.getText().trim();
                String drType = (String) drTypeCombo.getSelectedItem();
                String crAcc = crAccountField.getText().trim();
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
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                List<EntryLine> lines = new ArrayList<>();
                lines.add(new EntryLine(drAcc, drType, amount, 0.0));
                lines.add(new EntryLine(crAcc, crType, 0.0, amount));

                JournalRecord record = new JournalRecord(counter++, date, desc, lines);
                allEntries.add(record);
                saveJournalsToFile();

                for (EntryLine line : lines) {
                    tableModel.addRow(new Object[]{record.id, record.date, record.description, line.account, line.type, line.debit, line.credit});
                }

                JOptionPane.showMessageDialog(this, "Transaction Confirmed!");
                descArea.setText("");
                amtField.setText("");
                drAccountField.setText("");
                crAccountField.setText("");
                dateField.setText(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public String getCurrentJournalFile() {
        return currentJournalFile;
    }

    private void populateExistingTableRows() {
        for (JournalRecord record : allEntries) {
            for (EntryLine line : record.lines) {
                tableModel.addRow(new Object[]{record.id, record.date, record.description, line.account, line.type, line.debit, line.credit});
            }
        }
    }

    public void loadJournalsFromFile() {
        File file = new File(currentJournalFile);
        if (!file.exists()) file = new File("../" + currentJournalFile);
        if (!file.exists()) return;

        allEntries.clear();
        try (Scanner scanner = new Scanner(file)) {
            int maxId = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[0]);
                    if (id > maxId) maxId = id;
                    String date = parts[1];
                    String desc = parts[2];
                    List<EntryLine> lines = new ArrayList<>();
                    String[] lineItems = parts[3].split(";");
                    for (String item : lineItems) {
                        String[] fields = item.split(",");
                        if (fields.length >= 4) {
                            lines.add(new EntryLine(fields[0], fields[1], Double.parseDouble(fields[2]), Double.parseDouble(fields[3])));
                        }
                    }
                    allEntries.add(new JournalRecord(id, date, desc, lines));
                }
            }
            counter = maxId + 1;
        } catch (Exception e) {
            System.out.println("Error loading file.");
        }
    }

    private void saveJournalsToFile() {
        try (FileWriter writer = new FileWriter(currentJournalFile, false)) {
            for (JournalRecord rec : allEntries) {
                StringBuilder sb = new StringBuilder();
                sb.append(rec.id).append("|").append(rec.date).append("|").append(rec.description).append("|");
                for (int i = 0; i < rec.lines.size(); i++) {
                    EntryLine l = rec.lines.get(i);
                    sb.append(l.account).append(",").append(l.type).append(",").append(l.debit).append(",").append(l.credit);
                    if (i < rec.lines.size() - 1) sb.append(";");
                }
                writer.write(sb.toString() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving file.");
        }
    }
}