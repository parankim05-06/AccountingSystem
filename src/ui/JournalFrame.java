package ui;

import model.Account;
import model.Formats;
import model.JournalEntry;
import model.JournalLine;
import model.User;
import service.AppException;
import service.JournalService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * The JOURNAL ENTRY screen.
 *
 * What happens when you press ADD TRANSACTION:
 *   1. this screen reads the form and turns the text into a date and centavos
 *   2. it builds a JournalEntry with two lines (the debit line and the credit line)
 *   3. JournalService.saveDraft checks the rules and JournalDAO saves it to SQLite
 *   4. the table is reloaded from the database
 * An entry is saved as a DRAFT. Select it and press POST SELECTED ENTRY to send it
 * to the ledger and worksheet.
 */
public class JournalFrame extends JFrame {

    private final User user;
    private final JournalService journalService = new JournalService();

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField dateField;
    private JTextArea descriptionArea;
    private JComboBox<Account> debitCombo;
    private JComboBox<Account> creditCombo;
    private JTextField amountField;

    public JournalFrame(User user) {
        this.user = user;
        setTitle("Accounting System Workspace");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(Theme.WARM_CREAM);

        root.add(buildTopArea(), BorderLayout.NORTH);
        root.add(buildMainCard(), BorderLayout.CENTER);
        add(root);

        loadAccounts();
        loadTable();
    }

    // ------------------------------------------------------------ building the screen

    private JPanel buildTopArea() {
        JLabel moduleTitle = new JLabel("JOURNAL ENTRY");
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        moduleTitle.setForeground(Theme.DEEP_MOCHA);

        JButton refreshButton = Theme.primaryButton("Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        refreshButton.addActionListener(e -> {
            loadAccounts();
            loadTable();
            JOptionPane.showMessageDialog(this, "Journal entries refreshed!");
        });

        JButton homeButton = Theme.secondaryButton("\u2190 Dashboard");
        homeButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        homeButton.addActionListener(e -> {
            dispose();
            new HomeFrame(user).setVisible(true);
        });

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setBackground(Theme.WARM_CREAM);
        headerRight.add(refreshButton);
        headerRight.add(homeButton);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Theme.WARM_CREAM);
        topHeader.add(moduleTitle, BorderLayout.WEST);
        topHeader.add(headerRight, BorderLayout.EAST);

        JPanel topArea = new JPanel(new BorderLayout(0, 10));
        topArea.setBackground(Theme.WARM_CREAM);
        topArea.add(new NavBar(this, user, NavBar.JOURNAL), BorderLayout.NORTH);
        topArea.add(topHeader, BorderLayout.SOUTH);
        return topArea;
    }

    private JPanel buildMainCard() {
        JPanel mainCard = new JPanel(new BorderLayout(15, 15));
        mainCard.setBackground(Theme.SOFT_CARD);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.WARM_BEIGE, 1),
                new EmptyBorder(15, 20, 15, 20)));

        // ---- the table of saved entries ----
        String[] columns = {"ID", "Date", "Description", "Account", "Status", "Debit", "Credit"};
        tableModel = Theme.readOnlyTableModel(columns);
        table = new JTable(tableModel);
        table.setRowHeight(22);

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(5).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(6).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        table.getColumnModel().getColumn(4).setPreferredWidth(70);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(Theme.SOFT_CARD);
        formWrapper.add(buildForm(), BorderLayout.CENTER);
        formWrapper.add(buildButtonBar(), BorderLayout.SOUTH);

        mainCard.add(formWrapper, BorderLayout.NORTH);
        mainCard.add(new JScrollPane(table), BorderLayout.CENTER);
        return mainCard;
    }

    private JPanel buildForm() {
        dateField = new JTextField(Formats.todayDisplay(), 15);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        debitCombo = new JComboBox<>();
        creditCombo = new JComboBox<>();
        amountField = new JTextField(15);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SOFT_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        form.add(new JLabel("Date (DD-MM-YYYY):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        form.add(dateField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.1;
        form.add(new JLabel("Debit Account:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        form.add(debitCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4; gbc.gridheight = 2;
        form.add(new JScrollPane(descriptionArea), gbc);
        gbc.gridheight = 1;
        gbc.gridx = 2; gbc.weightx = 0.1;
        form.add(new JLabel("Credit Account:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        form.add(creditCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.1;
        form.add(new JLabel("Amount (\u20B1):"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        form.add(amountField, gbc);
        return form;
    }

    private JPanel buildButtonBar() {
        JButton clearButton = new JButton("CLEAR FORM");
        clearButton.setBackground(Color.WHITE);
        clearButton.setForeground(Theme.TEXT_DARK);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearForm());

        JButton postButton = Theme.secondaryButton("POST SELECTED ENTRY");
        postButton.addActionListener(e -> postSelectedEntry());

        JButton addButton = Theme.primaryButton("ADD TRANSACTION");
        addButton.addActionListener(e -> addTransaction());

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonBar.setBackground(Theme.SOFT_CARD);
        buttonBar.add(clearButton);
        buttonBar.add(postButton);
        buttonBar.add(addButton);
        return buttonBar;
    }

    // ------------------------------------------------------------ loading data

    /** Fills both drop-downs from the accounts table. */
    private void loadAccounts() {
        try {
            List<Account> accounts = journalService.getActiveAccounts();
            debitCombo.removeAllItems();
            creditCombo.removeAllItems();
            debitCombo.addItem(Account.placeholder());
            creditCombo.addItem(Account.placeholder());
            for (Account account : accounts) {
                debitCombo.addItem(account);
                creditCombo.addItem(account);
            }
        } catch (AppException ex) {
            showError(ex.getMessage());
        }
    }

    /** Reloads the table from SQLite: one table row for each debit/credit line. */
    private void loadTable() {
        tableModel.setRowCount(0);
        try {
            for (JournalEntry entry : journalService.getAllEntries()) {
                for (JournalLine line : entry.getLines()) {
                    tableModel.addRow(new Object[]{
                        entry.getId(),
                        Formats.isoToDisplayDate(entry.getEntryDate()),
                        entry.getDescription(),
                        line.getAccountCode() + " - " + line.getAccountName(),
                        entry.getStatus(),
                        Formats.moneyOrBlank(line.getDebit()),
                        Formats.moneyOrBlank(line.getCredit())
                    });
                }
            }
        } catch (AppException ex) {
            showError(ex.getMessage());
        }
    }

    // ------------------------------------------------------------ button actions

    private void addTransaction() {
        Account debitAccount = (Account) debitCombo.getSelectedItem();
        Account creditAccount = (Account) creditCombo.getSelectedItem();

        if (debitAccount == null || creditAccount == null
                || debitAccount.getId() == 0 || creditAccount.getId() == 0) {
            showWarning("Please select an account.");
            return;
        }
        if (debitAccount.getId() == creditAccount.getId()) {
            showWarning("Debit and Credit accounts must be different.");
            return;
        }

        try {
            // Step 1: turn the typed text into real values (or explain what is wrong)
            String isoDate = Formats.displayToIsoDate(dateField.getText());
            long amount = Formats.parseMoney(amountField.getText());

            // Step 2: build the entry: one debit line and one credit line of the same amount
            JournalEntry entry = new JournalEntry();
            entry.setEntryDate(isoDate);
            entry.setDescription(descriptionArea.getText().trim());
            entry.addLine(new JournalLine(debitAccount.getId(), amount, 0));
            entry.addLine(new JournalLine(creditAccount.getId(), 0, amount));

            // Step 3: the service checks the rules and the DAO saves it
            int newId = journalService.saveDraft(entry);

            // Step 4: show what is now in the database
            loadTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Transaction saved as entry #" + newId + " (DRAFT).\n"
                    + "Select any of its rows in the table and press POST SELECTED ENTRY\n"
                    + "to send it to the ledger.");
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage()); // a bad date or amount
        } catch (AppException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void postSelectedEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showWarning("Please select a journal entry row in the table first.");
            return;
        }
        int entryId = (Integer) tableModel.getValueAt(selectedRow, 0);

        try {
            journalService.postEntry(entryId);
            loadTable();
            JOptionPane.showMessageDialog(this, "Entry #" + entryId + " has been posted. It now appears in the ledger.");
        } catch (AppException ex) {
            showWarning(ex.getMessage());
        }
    }

    private void clearForm() {
        descriptionArea.setText("");
        amountField.setText("");
        debitCombo.setSelectedIndex(0);
        creditCombo.setSelectedIndex(0);
        dateField.setText(Formats.todayDisplay());
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
