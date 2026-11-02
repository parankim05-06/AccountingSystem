package ui;

import model.Formats;
import model.JournalLine;
import model.User;
import service.AppException;
import service.JournalService;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The LEDGER screen: one "T-account" box per account, debits on the left and
 * credits on the right. The numbers come from POSTED journal lines in SQLite,
 * grouped by account in JournalService.getLedger().
 */
public class LedgerFrame extends JFrame {

    private final User user;
    private final JournalService journalService = new JournalService();
    private JPanel containerPanel;

    public LedgerFrame(User user) {
        this.user = user;
        setTitle("Accounting System Workspace");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(Theme.WARM_CREAM);

        root.add(buildTopArea(), BorderLayout.NORTH);

        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(Theme.WARM_CREAM);
        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        root.add(scrollPane, BorderLayout.CENTER);
        add(root);

        buildLedgerTables();
    }

    private JPanel buildTopArea() {
        JLabel moduleTitle = new JLabel("LEDGER");
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        moduleTitle.setForeground(Theme.DEEP_MOCHA);

        JButton homeButton = Theme.secondaryButton("\u2190 Dashboard");
        homeButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        homeButton.addActionListener(e -> {
            dispose();
            new HomeFrame(user).setVisible(true);
        });

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setBackground(Theme.WARM_CREAM);
        leftHeader.add(homeButton);
        leftHeader.add(moduleTitle);

        JButton refreshButton = Theme.primaryButton("Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        refreshButton.addActionListener(e -> {
            buildLedgerTables();
            JOptionPane.showMessageDialog(this, "Ledger refreshed with latest entries!");
        });

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(Theme.WARM_CREAM);
        filterPanel.add(refreshButton);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Theme.WARM_CREAM);
        topHeader.add(leftHeader, BorderLayout.WEST);
        topHeader.add(filterPanel, BorderLayout.EAST);

        JPanel topArea = new JPanel(new BorderLayout(0, 10));
        topArea.setBackground(Theme.WARM_CREAM);
        topArea.add(new NavBar(this, user, NavBar.LEDGER), BorderLayout.NORTH);
        topArea.add(topHeader, BorderLayout.SOUTH);
        return topArea;
    }

    /** Reads the ledger from the service and draws one box per account. */
    private void buildLedgerTables() {
        containerPanel.removeAll();

        Map<String, List<JournalLine>> ledger;
        try {
            ledger = journalService.getLedger();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ledger.isEmpty()) {
            JLabel noData = new JLabel("No ledger entries found. Post a journal entry first.");
            noData.setFont(new Font("SansSerif", Font.ITALIC, 14));
            containerPanel.add(noData);
        }

        for (Map.Entry<String, List<JournalLine>> account : ledger.entrySet()) {
            containerPanel.add(buildAccountBox(account.getKey(), account.getValue()));
            containerPanel.add(Box.createVerticalStrut(10));
        }

        containerPanel.revalidate();
        containerPanel.repaint();
    }

    /** One account: debit lines on the left, credit lines on the right, then TOTAL and BALANCE. */
    private JPanel buildAccountBox(String accountTitle, List<JournalLine> lines) {
        List<JournalLine> debits = new ArrayList<>();
        List<JournalLine> credits = new ArrayList<>();
        long totalDebit = 0;
        long totalCredit = 0;
        for (JournalLine line : lines) {
            if (line.getDebit() > 0) {
                debits.add(line);
                totalDebit += line.getDebit();
            }
            if (line.getCredit() > 0) {
                credits.add(line);
                totalCredit += line.getCredit();
            }
        }

        String[] columns = {"Date", "Explanation", "Debit", "Date", "Explanation", "Credit"};
        DefaultTableModel model = Theme.readOnlyTableModel(columns);

        int rowCount = Math.max(debits.size(), credits.size());
        for (int i = 0; i < rowCount; i++) {
            JournalLine debit = i < debits.size() ? debits.get(i) : null;
            JournalLine credit = i < credits.size() ? credits.get(i) : null;
            model.addRow(new Object[]{
                debit == null ? "" : Formats.isoToDisplayDate(debit.getEntryDate()),
                debit == null ? "" : debit.getEntryDescription(),
                debit == null ? "" : Formats.money(debit.getDebit()),
                credit == null ? "" : Formats.isoToDisplayDate(credit.getEntryDate()),
                credit == null ? "" : credit.getEntryDescription(),
                credit == null ? "" : Formats.money(credit.getCredit())
            });
        }

        model.addRow(new Object[]{"", "TOTAL", Formats.money(totalDebit), "", "", Formats.money(totalCredit)});
        if (totalDebit >= totalCredit) {
            model.addRow(new Object[]{"", "BALANCE", Formats.money(totalDebit - totalCredit), "", "", ""});
        } else {
            model.addRow(new Object[]{"", "BALANCE", "", "", "", Formats.money(totalCredit - totalDebit)});
        }

        JTable table = new JTable(model);
        table.setRowHeight(20);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(2).setCellRenderer(rightAlign);
        table.getColumnModel().getColumn(5).setCellRenderer(rightAlign);

        JLabel titleLabel = new JLabel("Account Title: " + accountTitle);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleLabel.setForeground(Theme.DEEP_MOCHA);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(880, 100));

        JPanel accountPanel = new JPanel(new BorderLayout(5, 5));
        accountPanel.setBackground(Color.WHITE);
        accountPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.CARD_BORDER),
                new EmptyBorder(8, 10, 8, 10)));
        accountPanel.setMaximumSize(new Dimension(920, 220));
        accountPanel.add(titleLabel, BorderLayout.NORTH);
        accountPanel.add(tableScroll, BorderLayout.CENTER);
        return accountPanel;
    }
}
