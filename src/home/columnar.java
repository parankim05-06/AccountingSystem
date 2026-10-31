
package home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class columnar extends JPanel {

    private journal journalRef;
    private String username;
    private DefaultTableModel tableModel;
    private JTable table;

    private int accountRowCount = 0;
    private boolean rebuildingWorksheet = false;

    private JLabel adjustmentStatusLabel =
            new JLabel("ADJUSTMENTS VERIFICATION: NOT CHECKED");

    private JLabel balanceStatusLabel =
            new JLabel("BALANCE SHEET VERIFICATION: NOT CHECKED");

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
    private final Color SUCCESS_GREEN =
            new Color(34, 139, 34);

    public columnar(journal journalRef, String username) {

        this.journalRef = journalRef;
        this.username = username;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(WARM_CREAM);

        // TOP NAVIGATION BAR
        JPanel navBar =
                new JPanel(new GridLayout(1, 3, 10, 0));

        navBar.setBackground(WARM_CREAM);
        navBar.setPreferredSize(new Dimension(0, 40));

        JButton journalTab =
                new JButton("JOURNAL ENTRY");

        styleNavigationButton(journalTab, false);

        journalTab.addActionListener(e -> {

            journalRef.loadJournalsFromFile();

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
            frame.add(journalRef);
            frame.setVisible(true);
        });

        JButton ledgerTab = new JButton("LEDGER");
        styleNavigationButton(ledgerTab, false);

        ledgerTab.addActionListener(e -> {

            journalRef.loadJournalsFromFile();

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
            frame.add(new ledger(journalRef, username));
            frame.setVisible(true);
        });

        JButton columnarTab =
                new JButton("12 COLUMNAR SHEET");

        styleNavigationButton(columnarTab, true);

        navBar.add(journalTab);
        navBar.add(ledgerTab);
        navBar.add(columnarTab);

        // HEADER
        JLabel moduleTitle =
                new JLabel("12 COLUMNAR SHEET");

        moduleTitle.setFont(
                new Font("SansSerif", Font.BOLD, 16)
        );

        moduleTitle.setForeground(DEEP_MOCHA);

        JPanel headerRight =
                new JPanel(new FlowLayout(
                        FlowLayout.RIGHT, 10, 0
                ));

        headerRight.setBackground(WARM_CREAM);

        JButton refreshBtn = new JButton("Refresh");

        refreshBtn.setBackground(DEEP_MOCHA);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(
                new Font("SansSerif", Font.BOLD, 11)
        );
        refreshBtn.setFocusPainted(false);

        refreshBtn.addActionListener(e -> {

            journalRef.loadJournalsFromFile();
            rebuildWorksheet();

            JOptionPane.showMessageDialog(
                    this,
                    "12 COLUMNAR SHEET REFRESHED!"
            );
        });

        // EDIT ADJUSTMENT BUTTON
        JButton editAdjustmentBtn =
                new JButton("EDIT ADJUSTMENT");

        editAdjustmentBtn.setBackground(DEEP_MOCHA);
        editAdjustmentBtn.setForeground(Color.WHITE);
        editAdjustmentBtn.setFont(
                new Font("SansSerif", Font.BOLD, 11)
        );
        editAdjustmentBtn.setFocusPainted(false);

        editAdjustmentBtn.addActionListener(
                e -> editAdjustment()
        );

        JButton homeBtn =
                new JButton("← DASHBOARD");

        homeBtn.setFont(
                new Font("SansSerif", Font.BOLD, 11)
        );

        homeBtn.setForeground(TEXT_DARK);
        homeBtn.setBackground(WARM_BEIGE);
        homeBtn.setFocusPainted(false);

        homeBtn.addActionListener(e -> {

            Window window =
                    SwingUtilities.getWindowAncestor(this);

            if (window != null) {
                window.dispose();
            }

            new homepage(username).showDashboard();
        });

        headerRight.add(refreshBtn);
        headerRight.add(editAdjustmentBtn);
        headerRight.add(homeBtn);

        JPanel topHeader =
                new JPanel(new BorderLayout());

        topHeader.setBackground(WARM_CREAM);
        topHeader.add(moduleTitle, BorderLayout.WEST);
        topHeader.add(headerRight, BorderLayout.EAST);

        JPanel topContainer =
                new JPanel(new BorderLayout(0, 10));

        topContainer.setBackground(WARM_CREAM);
        topContainer.add(navBar, BorderLayout.NORTH);
        topContainer.add(topHeader, BorderLayout.SOUTH);

        // TABLE PANEL
        JPanel mainCard =
                new JPanel(new BorderLayout());

        mainCard.setBackground(SOFT_CARD);

        mainCard.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                WARM_BEIGE, 1
                        ),
                        new EmptyBorder(15, 15, 15, 15)
                )
        );

        String[] columns = {

                "ACCOUNT TITLE",
                "TRIAL BALANCE DEBIT",
                "TRIAL BALANCE CREDIT",
                "ADJUSTMENTS DEBIT",
                "ADJUSTMENTS CREDIT",
                "ADJUSTED TRIAL BALANCE DEBIT",
                "ADJUSTED TRIAL BALANCE CREDIT",
                "INCOME STATEMENT DEBIT",
                "INCOME STATEMENT CREDIT",
                "OWNER'S EQUITY DEBIT",
                "OWNER'S EQUITY CREDIT",
                "BALANCE SHEET DEBIT",
                "BALANCE SHEET CREDIT"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row, int column
            ) {
                return false;
            }
        };

        table = new JTable(tableModel);

        table.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF
        );

        table.setRowHeight(24);

        table.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 11)
        );

        DefaultTableCellRenderer rightAlign =
                new DefaultTableCellRenderer();

        rightAlign.setHorizontalAlignment(
                JLabel.RIGHT
        );

        DefaultTableCellRenderer centerAlign =
                new DefaultTableCellRenderer();

        centerAlign.setHorizontalAlignment(
                JLabel.CENTER
        );

        table.getColumnModel().getColumn(0)
                .setCellRenderer(centerAlign);

        table.getColumnModel().getColumn(0)
                .setPreferredWidth(180);

        for (int i = 1; i < columns.length; i++) {

            table.getColumnModel().getColumn(i)
                    .setCellRenderer(rightAlign);

            table.getColumnModel().getColumn(i)
                    .setPreferredWidth(180);
        }

        JScrollPane scrollPane =
                new JScrollPane(
                        table,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
                );

        mainCard.add(scrollPane, BorderLayout.CENTER);

        // VERIFICATION LABELS
        JPanel verificationPanel =
                new JPanel(new GridLayout(2, 1, 5, 5));

        verificationPanel.setBackground(SOFT_CARD);

        verificationPanel.setBorder(
                new EmptyBorder(10, 5, 5, 5)
        );

        adjustmentStatusLabel.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        balanceStatusLabel.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        adjustmentStatusLabel.setForeground(DEEP_MOCHA);
        balanceStatusLabel.setForeground(DEEP_MOCHA);

        verificationPanel.add(adjustmentStatusLabel);
        verificationPanel.add(balanceStatusLabel);

        mainCard.add(
                verificationPanel,
                BorderLayout.SOUTH
        );

        add(topContainer, BorderLayout.NORTH);
        add(mainCard, BorderLayout.CENTER);

        rebuildWorksheet();
    }

    // STYLE NAVIGATION BUTTONS
    private void styleNavigationButton(
            JButton button,
            boolean selected
    ) {

        button.setBackground(
                selected ? DEEP_MOCHA : WARM_BEIGE
        );

        button.setForeground(
                selected ? Color.WHITE : TEXT_DARK
        );

        button.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        button.setFocusPainted(false);
    }

    // REBUILD WORKSHEET
    private void rebuildWorksheet() {

        if (tableModel == null) {
            return;
        }

        rebuildingWorksheet = true;

        try {

            tableModel.setRowCount(0);
            populateWorksheetData();

        } finally {

            rebuildingWorksheet = false;
        }
    }

    // FORMAT AMOUNTS
    private String money(double amount) {

        if (Math.abs(amount) < 0.005) {
            return "";
        }

        return String.format("%.2f", amount);
    }

    // NORMALIZE ACCOUNT TITLES
    private String normalizeAccountName(String account) {

        return account == null
                ? ""
                : account.trim().toUpperCase();
    }

    // IDENTIFY DRAWING ACCOUNTS
    private boolean isDrawingAccount(String account) {

        return normalizeAccountName(account)
                .contains("DRAWING");
    }

    // UPDATE VERIFICATION LABEL
    private void updateVerificationLabel(
            JLabel label,
            String title,
            boolean balanced
    ) {

        if (balanced) {

            label.setText(title + ": BALANCED");
            label.setForeground(SUCCESS_GREEN);

        } else {

            label.setText(title + ": NOT BALANCED");
            label.setForeground(Color.RED);
        }
    }

    // POPULATE WORKSHEET
    private void populateWorksheetData() {

        accountRowCount = 0;

        Map<String, Double> trialDr =
                new HashMap<>();

        Map<String, Double> trialCr =
                new HashMap<>();

        Map<String, Double> adjustmentDr =
                new HashMap<>();

        Map<String, Double> adjustmentCr =
                new HashMap<>();

        Map<String, String> accountTypes =
                new HashMap<>();

        // COLLECT ACTIVE REGULAR AND ADJUSTING ENTRIES
        for (journal.JournalRecord rec :
                journalRef.allEntries) {

            if (rec.isVoided) {
                continue;
            }

            for (journal.EntryLine line : rec.lines) {

                if (line.account == null
                        || line.account.trim().isEmpty()) {
                    continue;
                }

                String account =
                        normalizeAccountName(line.account);

                String type = line.type != null
                        ? line.type.trim().toUpperCase()
                        : "ASSET";

                accountTypes.put(account, type);

                // ADJUSTING ENTRY
                if (rec.isAdjusting) {

                    if (line.debit > 0) {

                        adjustmentDr.put(
                                account,
                                adjustmentDr.getOrDefault(
                                        account, 0.0
                                ) + line.debit
                        );
                    }

                    if (line.credit > 0) {

                        adjustmentCr.put(
                                account,
                                adjustmentCr.getOrDefault(
                                        account, 0.0
                                ) + line.credit
                        );
                    }

                } else {

                    // REGULAR ENTRY
                    if (line.debit > 0) {

                        trialDr.put(
                                account,
                                trialDr.getOrDefault(
                                        account, 0.0
                                ) + line.debit
                        );
                    }

                    if (line.credit > 0) {

                        trialCr.put(
                                account,
                                trialCr.getOrDefault(
                                        account, 0.0
                                ) + line.credit
                        );
                    }
                }
            }
        }

        // SORT ACCOUNTS BY CLASSIFICATION
        List<String> accounts =
                new ArrayList<>(accountTypes.keySet());

        Map<String, Integer> classificationOrder =
                new HashMap<>();

        classificationOrder.put("ASSET", 1);
        classificationOrder.put("LIABILITY", 2);
        classificationOrder.put("EQUITY", 3);
        classificationOrder.put("CAPITAL", 3);
        classificationOrder.put("REVENUE", 4);
        classificationOrder.put("INCOME", 4);
        classificationOrder.put("EXPENSE", 5);

        accounts.sort((a, b) -> {

            String typeA =
                    accountTypes.getOrDefault(a, "");

            String typeB =
                    accountTypes.getOrDefault(b, "");

            int orderA =
                    classificationOrder.getOrDefault(
                            typeA, 99
                    );

            int orderB =
                    classificationOrder.getOrDefault(
                            typeB, 99
                    );

            if (orderA != orderB) {
                return Integer.compare(orderA, orderB);
            }

            return a.compareToIgnoreCase(b);
        });

        // WORKSHEET TOTALS
        double totalTbDr = 0;
        double totalTbCr = 0;

        double totalAdjDr = 0;
        double totalAdjCr = 0;

        double totalAtbDr = 0;
        double totalAtbCr = 0;

        double totalIsDr = 0;
        double totalIsCr = 0;

        double totalOeDr = 0;
        double totalOeCr = 0;

        double totalBsDr = 0;
        double totalBsCr = 0;

        // PROCESS EACH ACCOUNT
        for (String account : accounts) {

            double originalDr =
                    trialDr.getOrDefault(account, 0.0);

            double originalCr =
                    trialCr.getOrDefault(account, 0.0);

            String type = accountTypes.get(account);

            // TRIAL BALANCE
            double trialNet =
                    originalDr - originalCr;

            double tbDr =
                    Math.max(trialNet, 0);

            double tbCr =
                    Math.max(-trialNet, 0);

            totalTbDr += tbDr;
            totalTbCr += tbCr;

            // ADJUSTMENTS
            double adjDr =
                    adjustmentDr.getOrDefault(
                            account, 0.0
                    );

            double adjCr =
                    adjustmentCr.getOrDefault(
                            account, 0.0
                    );

            totalAdjDr += adjDr;
            totalAdjCr += adjCr;

            // ADJUSTED TRIAL BALANCE
            double adjustedNet =
                    trialNet + adjDr - adjCr;

            double atbDr =
                    Math.max(adjustedNet, 0);

            double atbCr =
                    Math.max(-adjustedNet, 0);

            totalAtbDr += atbDr;
            totalAtbCr += atbCr;

            // CLASSIFICATION COLUMNS
            double isDr = 0;
            double isCr = 0;

            double oeDr = 0;
            double oeCr = 0;

            double bsDr = 0;
            double bsCr = 0;

            if (type.equals("INCOME")
                    || type.equals("REVENUE")) {

                isDr = atbDr;
                isCr = atbCr;

            } else if (type.equals("EXPENSE")) {

                isDr = atbDr;
                isCr = atbCr;

            } else if (isDrawingAccount(account)) {

                oeDr = atbDr;
                oeCr = atbCr;

            } else if (type.equals("EQUITY")
                    || type.equals("CAPITAL")) {

                oeDr = atbDr;
                oeCr = atbCr;

            } else if (type.equals("ASSET")
                    || type.equals("LIABILITY")) {

                bsDr = atbDr;
                bsCr = atbCr;
            }

            totalIsDr += isDr;
            totalIsCr += isCr;

            totalOeDr += oeDr;
            totalOeCr += oeCr;

            totalBsDr += bsDr;
            totalBsCr += bsCr;

            // ADD ACCOUNT ROW
            tableModel.addRow(new Object[]{

                    account,

                    money(tbDr),
                    money(tbCr),

                    money(adjDr),
                    money(adjCr),

                    money(atbDr),
                    money(atbCr),

                    money(isDr),
                    money(isCr),

                    money(oeDr),
                    money(oeCr),

                    money(bsDr),
                    money(bsCr)
            });

            accountRowCount++;
        }

        // NET INCOME OR NET LOSS
        double netResult =
                totalIsCr - totalIsDr;

        double netIncome =
                Math.max(netResult, 0);

        double netLoss =
                Math.max(-netResult, 0);

        // ENDING OWNER'S CAPITAL
        double endingCapital =
                totalOeCr - totalOeDr + netResult;

        // TOTAL ROW
        tableModel.addRow(new Object[]{

                "TOTAL",

                money(totalTbDr),
                money(totalTbCr),

                money(totalAdjDr),
                money(totalAdjCr),

                money(totalAtbDr),
                money(totalAtbCr),

                money(totalIsDr),
                money(totalIsCr),

                money(totalOeDr),
                money(totalOeCr),

                money(totalBsDr),
                money(totalBsCr)
        });

        // NET INCOME ROW
        tableModel.addRow(new Object[]{

                "NET INCOME",

                "", "", "", "", "", "",

                money(netIncome),
                money(netLoss),

                money(netLoss),
                money(netIncome),

                "", ""
        });

        // ENDING OWNER'S CAPITAL
        tableModel.addRow(new Object[]{

                "ENDING OWNER'S CAPITAL",

                "", "", "", "", "", "", "", "",

                money(endingCapital),
                "",

                "",
                money(endingCapital)
        });

        // GRAND TOTALS
        double grandIsDr =
                totalIsDr + netIncome;

        double grandIsCr =
                totalIsCr + netLoss;

        double grandOeDr =
                totalOeDr + netLoss + endingCapital;

        double grandOeCr =
                totalOeCr + netIncome;

        double grandBsDr = totalBsDr;
        double grandBsCr =
                totalBsCr + endingCapital;

        // VERIFY ADJUSTMENTS
        boolean adjustmentsBalanced =
                isAdjustmentBalanced(
                        totalAdjDr, totalAdjCr
                );

        updateVerificationLabel(
                adjustmentStatusLabel,
                "ADJUSTMENTS VERIFICATION",
                adjustmentsBalanced
        );

        // VERIFY BALANCE SHEET
        boolean balanceSheetBalanced =
                isBalanceSheetBalanced(
                        grandBsDr, grandBsCr
                );

        updateVerificationLabel(
                balanceStatusLabel,
                "BALANCE SHEET VERIFICATION",
                balanceSheetBalanced
        );

        // GRAND TOTAL ROW
        tableModel.addRow(new Object[]{

                "GRAND TOTAL",

                "", "", "", "", "", "",

                money(grandIsDr),
                money(grandIsCr),

                money(grandOeDr),
                money(grandOeCr),

                money(grandBsDr),
                money(grandBsCr)
        });
    }

    // VERIFY ADJUSTMENTS
    private boolean isAdjustmentBalanced(
            double adjustmentDebit,
            double adjustmentCredit
    ) {

        return Math.abs(
                adjustmentDebit - adjustmentCredit
        ) < 0.01;
    }

    // VERIFY BALANCE SHEET
    private boolean isBalanceSheetBalanced(
            double balanceSheetDebit,
            double balanceSheetCredit
    ) {

        return Math.abs(
                balanceSheetDebit - balanceSheetCredit
        ) < 0.01;
    }

    // EDIT ADJUSTING ENTRY FROM COLUMNAR
    private void editAdjustment() {

        List<journal.JournalRecord> adjustments =
                new ArrayList<>();

        for (journal.JournalRecord record :
                journalRef.allEntries) {

            if (record.isAdjusting && !record.isVoided) {
                adjustments.add(record);
            }
        }

        if (adjustments.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "There are no active Adjusting Entries to edit.",
                    "No Adjustments",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        // CREATE ADJUSTMENT SELECTION LIST
        String[] options =
                new String[adjustments.size()];

        for (int i = 0; i < adjustments.size(); i++) {

            journal.JournalRecord record =
                    adjustments.get(i);

            options[i] =
                    "ID: " + record.id
                            + " | " + record.date
                            + " | " + record.description;
        }

        JComboBox<String> adjustmentSelector =
                new JComboBox<>(options);

        int selection = JOptionPane.showConfirmDialog(
                this,
                adjustmentSelector,
                "SELECT ADJUSTING ENTRY",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (selection != JOptionPane.OK_OPTION) {
            return;
        }

        int selectedIndex =
                adjustmentSelector.getSelectedIndex();

        if (selectedIndex < 0) {
            return;
        }

        journal.JournalRecord record =
                adjustments.get(selectedIndex);

        // IDENTIFY DEBIT AND CREDIT LINES
        journal.EntryLine debitLine = null;
        journal.EntryLine creditLine = null;

        for (journal.EntryLine line : record.lines) {

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
                    "This adjustment must contain one debit and one credit line.",
                    "Cannot Edit",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        // EDIT FIELDS
        JTextField dateField =
                new JTextField(record.date);

        JTextField descriptionField =
                new JTextField(record.description);

        JTextField debitAccountField =
                new JTextField(debitLine.account);

        JTextField creditAccountField =
                new JTextField(creditLine.account);

        String[] types = {
                "ASSET",
                "EQUITY",
                "EXPENSE",
                "LIABILITY",
                "REVENUE"
        };

        JComboBox<String> debitType =
                new JComboBox<>(types);

        JComboBox<String> creditType =
                new JComboBox<>(types);

        debitType.setSelectedItem(debitLine.type);
        creditType.setSelectedItem(creditLine.type);

        JTextField amountField =
                new JTextField(
                        String.valueOf(debitLine.debit)
                );

        JPanel panel =
                new JPanel(new GridLayout(7, 2, 8, 8));

        panel.add(new JLabel("Date (DD-MM-YYYY):"));
        panel.add(dateField);

        panel.add(new JLabel("Description:"));
        panel.add(descriptionField);

        panel.add(new JLabel("Debit Account:"));
        panel.add(debitAccountField);

        panel.add(new JLabel("Debit Account Type:"));
        panel.add(debitType);

        panel.add(new JLabel("Credit Account:"));
        panel.add(creditAccountField);

        panel.add(new JLabel("Credit Account Type:"));
        panel.add(creditType);

        panel.add(new JLabel("Amount:"));
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "EDIT ADJUSTING ENTRY ID " + record.id,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // VALIDATE AND SAVE
        try {

            String date =
                    dateField.getText().trim();

            String description =
                    descriptionField.getText().trim();

            String debitAccount =
                    debitAccountField.getText().trim();

            String creditAccount =
                    creditAccountField.getText().trim();

            double amount =
                    Double.parseDouble(
                            amountField.getText().trim()
                    );

            if (date.isEmpty()
                    || description.isEmpty()
                    || debitAccount.isEmpty()
                    || creditAccount.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please complete all fields.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            if (debitAccount.equalsIgnoreCase(
                    creditAccount
            )) {

                JOptionPane.showMessageDialog(
                        this,
                        "Debit and credit accounts must be different.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

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

            List<journal.EntryLine> updatedLines =
                    new ArrayList<>();

            updatedLines.add(
                    new journal.EntryLine(
                            debitAccount,
                            (String) debitType.getSelectedItem(),
                            amount,
                            0.0
                    )
            );

            updatedLines.add(
                    new journal.EntryLine(
                            creditAccount,
                            (String) creditType.getSelectedItem(),
                            0.0,
                            amount
                    )
            );

            // UPDATE THE ORIGINAL JOURNAL RECORD
            record.date = date;
            record.description = description;
            record.lines = updatedLines;

            // SAVE TO JOURNAL FILE
            journalRef.saveChangesFromColumnar();

            // REFRESH COLUMNAR TOTALS
            rebuildWorksheet();

            JOptionPane.showMessageDialog(
                    this,
                    "Adjusting Entry ID " + record.id
                            + " has been updated successfully!",
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid numeric amount.",
                    "Invalid Amount",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
