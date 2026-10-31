
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

        public EntryLine(String account, String type,
                         double debit, double credit) {
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

        public JournalRecord(int id, String date, String description,
                             List<EntryLine> lines) {
            this(id, date, description, lines, false, "", false);
        }

        public JournalRecord(int id, String date, String description,
                             List<EntryLine> lines, boolean isVoided,
                             String voidReason) {
            this(id, date, description, lines, isVoided,
                    voidReason, false);
        }

        public JournalRecord(int id, String date, String description,
                             List<EntryLine> lines, boolean isVoided,
                             String voidReason, boolean isAdjusting) {
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
    private JTextField dateField;

    private final Color WARM_CREAM =
            new Color(248, 246, 242);
    private final Color SOFT_CARD =
            new Color(252, 250, 248);
    private final Color WARM_BEIGE =
            new Color(230, 225, 218);
    private final Color DEEP_MOCHA =
            new Color(92, 64, 51);
    private final Color TEXT_DARK =
            new Color(60, 50, 45);

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

        // TOP NAVIGATION
        JPanel navBar = new JPanel(
                new GridLayout(1, 3, 10, 0)
        );
        navBar.setBackground(WARM_CREAM);
        navBar.setPreferredSize(new Dimension(0, 40));

        JButton journalTab = new JButton("JOURNAL ENTRY");
        journalTab.setBackground(DEEP_MOCHA);
        journalTab.setForeground(Color.WHITE);
        journalTab.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        journalTab.setFocusPainted(false);

        JButton ledgerTab = new JButton("LEDGER");
        styleNavigationButton(ledgerTab);

        ledgerTab.addActionListener(e -> {
            loadJournalsFromFile();
            openWindow(new ledger(this, username));
        });

        JButton columnarTab =
                new JButton("12 COLUMNAR SHEET");
        styleNavigationButton(columnarTab);

        columnarTab.addActionListener(e -> {
            loadJournalsFromFile();
            openWindow(new columnar(this, username));
        });

        navBar.add(journalTab);
        navBar.add(ledgerTab);
        navBar.add(columnarTab);

        // HEADER
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(WARM_CREAM);

        JLabel moduleTitle = new JLabel("JOURNAL ENTRY");
        moduleTitle.setFont(
                new Font("SansSerif", Font.BOLD, 16)
        );
        moduleTitle.setForeground(DEEP_MOCHA);

        JPanel headerRight = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );
        headerRight.setBackground(WARM_CREAM);

        JButton refreshBtn = new JButton("Refresh");
        stylePrimaryButton(refreshBtn);

        refreshBtn.addActionListener(e -> {
            loadJournalsFromFile();
            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Journal entries refreshed!"
            );
        });

        JButton homeBtn = new JButton("← Dashboard");
        styleNavigationButton(homeBtn);

        homeBtn.addActionListener(e -> {
            Window window =
                    SwingUtilities.getWindowAncestor(this);

            if (window != null) {
                window.dispose();
            }

            new homepage(username).showDashboard();
        });

        headerRight.add(refreshBtn);
        headerRight.add(homeBtn);

        topHeader.add(moduleTitle, BorderLayout.WEST);
        topHeader.add(headerRight, BorderLayout.EAST);

        JPanel topContainer =
                new JPanel(new BorderLayout(0, 10));
        topContainer.setBackground(WARM_CREAM);
        topContainer.add(navBar, BorderLayout.NORTH);
        topContainer.add(topHeader, BorderLayout.SOUTH);

        // MAIN CARD
        JPanel mainCard =
                new JPanel(new BorderLayout(15, 15));
        mainCard.setBackground(SOFT_CARD);

        mainCard.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                WARM_BEIGE, 1
                        ),
                        new EmptyBorder(15, 20, 15, 20)
                )
        );

        JPanel formGrid =
                new JPanel(new GridBagLayout());
        formGrid.setBackground(SOFT_CARD);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        dateField = new JTextField(
                new SimpleDateFormat("dd-MM-yyyy")
                        .format(new Date()),
                15
        );

        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        JScrollPane descScroll =
                new JScrollPane(descArea);

        JTextField drAccountField =
                new JTextField(15);

        JTextField crAccountField =
                new JTextField(15);

        JTextField amtField =
                new JTextField(15);

        String[] accountTypes = {
                "ASSET",
                "EQUITY",
                "EXPENSE",
                "LIABILITY",
                "REVENUE"
        };

        JComboBox<String> drTypeCombo =
                new JComboBox<>(accountTypes);

        JComboBox<String> crTypeCombo =
                new JComboBox<>(accountTypes);

        String[] entryOptions = {
                "Regular Entry",
                "Adjusting Entry"
        };

        JComboBox<String> entryTypeCombo =
                new JComboBox<>(entryOptions);

        // FORM ROW 0
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.gridheight = 1;
        formGrid.add(
                new JLabel("Date (DD-MM-YYYY):"), gbc
        );

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        formGrid.add(dateField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.1;
        formGrid.add(
                new JLabel("Entry Category:"), gbc
        );

        gbc.gridx = 3;
        gbc.weightx = 0.4;
        formGrid.add(entryTypeCombo, gbc);

        // FORM ROW 1
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.1;
        formGrid.add(
                new JLabel("Description:"), gbc
        );

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        gbc.gridheight = 2;
        formGrid.add(descScroll, gbc);

        gbc.gridheight = 1;
        gbc.gridx = 2;
        gbc.weightx = 0.1;
        formGrid.add(
                new JLabel("Debit Account & Type:"), gbc
        );

        JPanel drPanel =
                new JPanel(new BorderLayout(5, 0));
        drPanel.setBackground(SOFT_CARD);
        drPanel.add(drAccountField, BorderLayout.CENTER);
        drPanel.add(drTypeCombo, BorderLayout.EAST);

        gbc.gridx = 3;
        gbc.weightx = 0.4;
        formGrid.add(drPanel, gbc);

        // FORM ROW 2
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        formGrid.add(
                new JLabel("Credit Account & Type:"), gbc
        );

        JPanel crPanel =
                new JPanel(new BorderLayout(5, 0));
        crPanel.setBackground(SOFT_CARD);
        crPanel.add(crAccountField, BorderLayout.CENTER);
        crPanel.add(crTypeCombo, BorderLayout.EAST);

        gbc.gridx = 3;
        gbc.weightx = 0.4;
        formGrid.add(crPanel, gbc);

        // FORM ROW 3
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 0.1;
        formGrid.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.4;
        formGrid.add(amtField, gbc);

        // BUTTONS
        JPanel btnBar =
                new JPanel(new FlowLayout(
                        FlowLayout.RIGHT, 10, 5
                ));

        btnBar.setBackground(SOFT_CARD);

        JButton clearBtn =
                new JButton("CLEAR FORM");

        JButton addBtn =
                new JButton("ADD TRANSACTION");

        JButton editBtn =
                new JButton("EDIT SELECTED");

        JButton voidBtn =
                new JButton("VOID SELECTED");

        clearBtn.setBackground(Color.WHITE);
        clearBtn.setForeground(TEXT_DARK);
        clearBtn.setFocusPainted(false);

        stylePrimaryButton(addBtn);
        stylePrimaryButton(editBtn);

        voidBtn.setBackground(
                new Color(150, 65, 55)
        );
        voidBtn.setForeground(Color.WHITE);
        voidBtn.setFocusPainted(false);

        btnBar.add(clearBtn);
        btnBar.add(addBtn);
        btnBar.add(editBtn);
        btnBar.add(voidBtn);

        JPanel formWrapper =
                new JPanel(new BorderLayout());

        formWrapper.setBackground(SOFT_CARD);
        formWrapper.add(formGrid, BorderLayout.CENTER);
        formWrapper.add(btnBar, BorderLayout.SOUTH);

        // JOURNAL TABLE
        String[] cols = {
                "ID",
                "Date",
                "Description",
                "Entry Category",
                "Account",
                "Type",
                "Debit",
                "Credit",
                "Status"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(
                    int row, int column
            ) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(22);

        populateExistingTableRows();

        JScrollPane tableScroll =
                new JScrollPane(table);

        tableScroll.setPreferredSize(
                new Dimension(0, 180)
        );

        mainCard.add(formWrapper, BorderLayout.NORTH);
        mainCard.add(tableScroll, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(mainCard, BorderLayout.CENTER);

        // TRACK EDITING RECORD
        final JournalRecord[] editingRecord = {null};

        // CLEAR FORM / CANCEL EDIT
        clearBtn.addActionListener(e -> {

            descArea.setText("");
            amtField.setText("");
            drAccountField.setText("");
            crAccountField.setText("");

            dateField.setText(
                    new SimpleDateFormat("dd-MM-yyyy")
                            .format(new Date())
            );

            entryTypeCombo.setSelectedIndex(0);
            entryTypeCombo.setEnabled(true);

            drTypeCombo.setSelectedIndex(0);
            crTypeCombo.setSelectedIndex(0);

            editingRecord[0] = null;
            addBtn.setText("ADD TRANSACTION");
        });

        // ADD OR SAVE EDITED ADJUSTMENT
        addBtn.addActionListener(e -> {

            try {

                String date = dateField.getText().trim();
                String desc = descArea.getText().trim();

                String drAcc =
                        drAccountField.getText().trim();

                String crAcc =
                        crAccountField.getText().trim();

                String drType =
                        (String) drTypeCombo.getSelectedItem();

                String crType =
                        (String) crTypeCombo.getSelectedItem();

                String amtText =
                        amtField.getText().trim();

                if (date.isEmpty()
                        || desc.isEmpty()
                        || drAcc.isEmpty()
                        || crAcc.isEmpty()
                        || amtText.isEmpty()) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Please fill out all fields.",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (drAcc.equalsIgnoreCase(crAcc)) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Debit and Credit accounts must be different.",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                double amount =
                        Double.parseDouble(amtText);

                if (amount <= 0
                        || Double.isInfinite(amount)
                        || Double.isNaN(amount)) {

                    JOptionPane.showMessageDialog(
                            this,
                            "Amount must be greater than zero.",
                            "Validation Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                List<EntryLine> updatedLines =
                        new ArrayList<>();

                updatedLines.add(
                        new EntryLine(
                                drAcc, drType, amount, 0.0
                        )
                );

                updatedLines.add(
                        new EntryLine(
                                crAcc, crType, 0.0, amount
                        )
                );

                if (editingRecord[0] != null) {

                    JournalRecord record =
                            editingRecord[0];

                    record.date = date;
                    record.description = desc;
                    record.lines = updatedLines;

                    saveChangesFromColumnar();

                    JOptionPane.showMessageDialog(
                            this,
                            "Adjusting Entry ID "
                                    + record.id
                                    + " has been updated successfully!"
                    );

                    editingRecord[0] = null;
                    addBtn.setText("ADD TRANSACTION");

                    entryTypeCombo.setEnabled(true);
                    entryTypeCombo.setSelectedIndex(0);

                } else {

                    boolean isAdjusting =
                            "Adjusting Entry".equals(
                                    entryTypeCombo.getSelectedItem()
                            );

                    JournalRecord record =
                            new JournalRecord(
                                    counter++,
                                    date,
                                    desc,
                                    updatedLines,
                                    false,
                                    "",
                                    isAdjusting
                            );

                    allEntries.add(record);

                    saveChangesFromColumnar();

                    JOptionPane.showMessageDialog(
                            this,
                            "Transaction Confirmed!"
                    );
                }

                refreshTable();

                descArea.setText("");
                amtField.setText("");
                drAccountField.setText("");
                crAccountField.setText("");

                dateField.setText(
                        new SimpleDateFormat("dd-MM-yyyy")
                                .format(new Date())
                );

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid numeric amount.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // LOAD SELECTED ADJUSTMENT FOR EDITING
        editBtn.addActionListener(e -> {

            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select an entry to edit.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int modelRow =
                    table.convertRowIndexToModel(selectedRow);

            int selectedId =
                    (int) tableModel.getValueAt(modelRow, 0);

            JournalRecord record =
                    findRecordById(selectedId);

            if (record == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Journal entry not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (!record.isAdjusting) {

                JOptionPane.showMessageDialog(
                        this,
                        "Only Adjusting Entries can be edited here.",
                        "Regular Entry",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (record.isVoided) {

                JOptionPane.showMessageDialog(
                        this,
                        "A voided entry cannot be edited.",
                        "Voided Entry",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (record.lines == null
                    || record.lines.size() < 2) {

                JOptionPane.showMessageDialog(
                        this,
                        "This entry does not contain enough account lines to edit.",
                        "Invalid Entry",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            EntryLine debitLine = null;
            EntryLine creditLine = null;

            for (EntryLine line : record.lines) {

                if (line.debit > 0) {
                    debitLine = line;
                }

                if (line.credit > 0) {
                    creditLine = line;
                }
            }

            if (debitLine == null || creditLine == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Could not identify the debit and credit lines.",
                        "Invalid Entry",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            editingRecord[0] = record;

            dateField.setText(record.date);
            descArea.setText(record.description);

            drAccountField.setText(debitLine.account);
            crAccountField.setText(creditLine.account);

            drTypeCombo.setSelectedItem(debitLine.type);
            crTypeCombo.setSelectedItem(creditLine.type);

            amtField.setText(
                    String.valueOf(debitLine.debit)
            );

            entryTypeCombo.setSelectedItem(
                    "Adjusting Entry"
            );

            entryTypeCombo.setEnabled(false);

            addBtn.setText("SAVE CHANGES");

            JOptionPane.showMessageDialog(
                    this,
                    "Edit the selected Adjusting Entry, then click SAVE CHANGES."
            );
        });

        // VOID SELECTED ENTRY
        voidBtn.addActionListener(e -> {

            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please select a journal entry to void.",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int modelRow =
                    table.convertRowIndexToModel(selectedRow);

            int selectedId =
                    (int) tableModel.getValueAt(modelRow, 0);

            JournalRecord selectedRecord =
                    findRecordById(selectedId);

            if (selectedRecord == null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Journal entry not found.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (selectedRecord.isVoided) {

                JOptionPane.showMessageDialog(
                        this,
                        "This transaction has already been voided.",
                        "Already Voided",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to void Journal Entry ID "
                            + selectedRecord.id + "?",
                    "Confirm Void Transaction",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            JTextArea reasonArea = new JTextArea(3, 25);
            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);

            int reasonResult = JOptionPane.showConfirmDialog(
                    this,
                    new JScrollPane(reasonArea),
                    "Enter Reason for Voiding",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (reasonResult != JOptionPane.OK_OPTION) {
                return;
            }

            String reason =
                    reasonArea.getText().trim();

            if (reason.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please provide a reason for voiding this transaction.",
                        "Reason Required",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            selectedRecord.isVoided = true;
            selectedRecord.voidReason = reason;

            if (editingRecord[0] == selectedRecord) {

                editingRecord[0] = null;
                addBtn.setText("ADD TRANSACTION");
                entryTypeCombo.setEnabled(true);
            }

            saveChangesFromColumnar();
            refreshTable();

            JOptionPane.showMessageDialog(
                    this,
                    "Journal Entry ID "
                            + selectedRecord.id
                            + " has been successfully voided."
            );
        });
    }

    // NAVIGATION BUTTON STYLE
    private void styleNavigationButton(JButton button) {

        button.setBackground(WARM_BEIGE);
        button.setForeground(TEXT_DARK);
        button.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );
        button.setFocusPainted(false);
    }

    // PRIMARY BUTTON STYLE
    private void stylePrimaryButton(JButton button) {

        button.setBackground(DEEP_MOCHA);
        button.setForeground(Color.WHITE);
        button.setFont(
                new Font("SansSerif", Font.BOLD, 11)
        );
        button.setFocusPainted(false);
    }

    // OPEN MODULE WINDOW
    private void openWindow(JPanel panel) {

        Window window =
                SwingUtilities.getWindowAncestor(this);

        if (window != null) {
            window.dispose();
        }

        JFrame frame =
                new JFrame("Accounting System Workspace");

        frame.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);
    }

    // FIND JOURNAL RECORD
    public JournalRecord findRecordById(int id) {

        for (JournalRecord record : allEntries) {

            if (record.id == id) {
                return record;
            }
        }

        return null;
    }

    public String getCurrentJournalFile() {
        return currentJournalFile;
    }

    // REFRESH TABLE
    private void refreshTable() {

        tableModel.setRowCount(0);
        populateExistingTableRows();
    }

    // POPULATE JOURNAL TABLE
    private void populateExistingTableRows() {

        for (JournalRecord record : allEntries) {

            String status =
                    record.isVoided ? "VOIDED" : "ACTIVE";

            String entryCategory =
                    record.isAdjusting
                            ? "Adjusting Entry"
                            : "Regular Entry";

            for (EntryLine line : record.lines) {

                tableModel.addRow(new Object[]{

                        record.id,
                        record.date,
                        record.description,
                        entryCategory,
                        line.account,
                        line.type,
                        line.debit,
                        line.credit,
                        status
                });
            }
        }
    }

    // LOAD JOURNALS FROM FILE
    public void loadJournalsFromFile() {

        File file = new File(currentJournalFile);

        if (!file.exists()) {
            file = new File("../" + currentJournalFile);
        }

        if (!file.exists()) {
            return;
        }

        allEntries.clear();

        try (Scanner scanner = new Scanner(file)) {

            int maxId = 0;

            while (scanner.hasNextLine()) {

                String line = scanner.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts =
                        line.split("\\|", -1);

                if (parts.length >= 4) {

                    int id = Integer.parseInt(parts[0]);
                    maxId = Math.max(maxId, id);

                    String date = parts[1];
                    String desc = parts[2];

                    List<EntryLine> lines =
                            new ArrayList<>();

                    String[] lineItems =
                            parts[3].split(";");

                    for (String item : lineItems) {

                        String[] fields =
                                item.split(",", -1);

                        if (fields.length >= 4) {

                            lines.add(
                                    new EntryLine(
                                            fields[0],
                                            fields[1],
                                            Double.parseDouble(fields[2]),
                                            Double.parseDouble(fields[3])
                                    )
                            );
                        }
                    }

                    boolean isVoided =
                            parts.length >= 5
                                    && Boolean.parseBoolean(parts[4]);

                    String voidReason =
                            parts.length >= 6
                                    ? parts[5] : "";

                    // OLD RECORDS DEFAULT TO REGULAR ENTRY
                    boolean isAdjusting =
                            parts.length >= 7
                                    && Boolean.parseBoolean(parts[6]);

                    allEntries.add(
                            new JournalRecord(
                                    id,
                                    date,
                                    desc,
                                    lines,
                                    isVoided,
                                    voidReason,
                                    isAdjusting
                            )
                    );
                }
            }

            counter = maxId + 1;

        } catch (Exception e) {

            System.out.println(
                    "Error loading file: " + e.getMessage()
            );
        }
    }

    // PUBLIC SAVE METHOD FOR COLUMNAR SHEET
    public void saveChangesFromColumnar() {
        saveJournalsToFile();
    }

    // SAVE JOURNALS TO FILE
    private void saveJournalsToFile() {

        try (FileWriter writer =
                     new FileWriter(currentJournalFile, false)) {

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

                    if (i < rec.lines.size() - 1) {
                        sb.append(";");
                    }
                }

                sb.append("|")
                        .append(rec.isVoided)
                        .append("|")
                        .append(rec.voidReason == null
                                ? "" : rec.voidReason)
                        .append("|")
                        .append(rec.isAdjusting);

                writer.write(sb.toString() + "\n");
            }

        } catch (IOException e) {

            System.out.println(
                    "Error saving file: " + e.getMessage()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to save journal file: "
                            + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
