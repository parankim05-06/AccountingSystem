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

public class ledger extends JPanel {
    private journal journalRef;
    private String username;
    private JPanel containerPanel;

    private final Color WARM_CREAM = new Color(248, 246, 242);
    private final Color WARM_BEIGE = new Color(230, 225, 218);
    private final Color DEEP_MOCHA = new Color(92, 64, 51);
    private final Color TEXT_DARK = new Color(60, 50, 45);

    public ledger(journal journalRef, String username) {
        this.journalRef = journalRef;
        this.username = username;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(WARM_CREAM);

        // --- TOP NAVIGATION BAR ---
        JPanel navBar = new JPanel(new GridLayout(1, 3, 10, 0));
        navBar.setBackground(WARM_CREAM);
        navBar.setPreferredSize(new Dimension(0, 40));

        JButton journalTab = new JButton("JOURNAL ENTRY");
        journalTab.setBackground(WARM_BEIGE);
        journalTab.setForeground(TEXT_DARK);
        journalTab.setFont(new Font("SansSerif", Font.BOLD, 12));
        journalTab.setFocusPainted(false);
        journalTab.addActionListener(e -> {
            journalRef.loadJournalsFromFile();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
            JFrame frame = new JFrame("Accounting System Workspace");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.add(journalRef);
            frame.setVisible(true);
        });

        JButton ledgerTab = new JButton("LEDGER");
        ledgerTab.setBackground(DEEP_MOCHA);
        ledgerTab.setForeground(Color.WHITE);
        ledgerTab.setFont(new Font("SansSerif", Font.BOLD, 12));
        ledgerTab.setFocusPainted(false);

        JButton columnarTab = new JButton("12 COLUMNAR SHEET");
        columnarTab.setBackground(WARM_BEIGE);
        columnarTab.setForeground(TEXT_DARK);
        columnarTab.setFont(new Font("SansSerif", Font.BOLD, 12));
        columnarTab.setFocusPainted(false);
        columnarTab.addActionListener(e -> {
            journalRef.loadJournalsFromFile();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
            JFrame frame = new JFrame("Accounting System Workspace");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new columnar(journalRef, username));
            frame.setVisible(true);
        });

        navBar.add(journalTab);
        navBar.add(ledgerTab);
        navBar.add(columnarTab);

        // --- HEADER ---
        JLabel moduleTitle = new JLabel("LEDGER");
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        moduleTitle.setForeground(DEEP_MOCHA);

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

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setBackground(WARM_CREAM);
        leftHeader.add(homeBtn);
        leftHeader.add(moduleTitle);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(WARM_CREAM);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(DEEP_MOCHA);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("SansSerif", Font.BOLD, 11));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            journalRef.loadJournalsFromFile();
            buildLedgerTables();
            JOptionPane.showMessageDialog(this, "Ledger refreshed with latest entries!");
        });

        filterPanel.add(refreshBtn);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(WARM_CREAM);
        topHeader.add(leftHeader, BorderLayout.WEST);
        topHeader.add(filterPanel, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setBackground(WARM_CREAM);
        topContainer.add(navBar, BorderLayout.NORTH);
        topContainer.add(topHeader, BorderLayout.SOUTH);

        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(WARM_CREAM);

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        buildLedgerTables();
    }

    private void buildLedgerTables() {
        containerPanel.removeAll();

        Map<String, List<LedgerItem>> accountEntries = new HashMap<>();

        for (journal.JournalRecord rec : journalRef.allEntries) {
            for (journal.EntryLine line : rec.lines) {
                accountEntries.putIfAbsent(line.account, new ArrayList<>());
                accountEntries.get(line.account).add(new LedgerItem(rec.date, rec.description, line.debit, line.credit));
            }
        }

        if (accountEntries.isEmpty()) {
            JLabel noData = new JLabel("No ledger entries found.");
            noData.setFont(new Font("SansSerif", Font.ITALIC, 14));
            containerPanel.add(noData);
        } else {
            for (Map.Entry<String, List<LedgerItem>> entry : accountEntries.entrySet()) {
                String accountTitle = entry.getKey();
                List<LedgerItem> items = entry.getValue();

                List<LedgerItem> debits = new ArrayList<>();
                List<LedgerItem> credits = new ArrayList<>();
                double totalDr = 0;
                double totalCr = 0;

                for (LedgerItem item : items) {
                    if (item.debit > 0) {
                        debits.add(item);
                        totalDr += item.debit;
                    }
                    if (item.credit > 0) {
                        credits.add(item);
                        totalCr += item.credit;
                    }
                }

                JPanel accountPanel = new JPanel(new BorderLayout(5, 5));
                accountPanel.setBackground(Color.WHITE);
                accountPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 215, 210)),
                    new EmptyBorder(8, 10, 8, 10)
                ));
                accountPanel.setMaximumSize(new Dimension(920, 220));

                JLabel titleLabel = new JLabel("Account Title: " + accountTitle);
                titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
                titleLabel.setForeground(DEEP_MOCHA);
                accountPanel.add(titleLabel, BorderLayout.NORTH);

                String[] cols = {"Date", "Explanation", "Debit", "Date", "Explanation", "Credit"};
                DefaultTableModel model = new DefaultTableModel(cols, 0);

                int maxRows = Math.max(debits.size(), credits.size());
                for (int i = 0; i < maxRows; i++) {
                    String drDate = i < debits.size() ? debits.get(i).date : "";
                    String drExp = i < debits.size() ? debits.get(i).explanation : "";
                    String drAmt = i < debits.size() ? String.format("%.2f", debits.get(i).debit) : "";

                    String crDate = i < credits.size() ? credits.get(i).date : "";
                    String crExp = i < credits.size() ? credits.get(i).explanation : "";
                    String crAmt = i < credits.size() ? String.format("%.2f", credits.get(i).credit) : "";

                    model.addRow(new Object[]{drDate, drExp, drAmt, crDate, crExp, crAmt});
                }

                model.addRow(new Object[]{
                    "", "TOTAL", String.format("%.2f", totalDr),
                    "", "", String.format("%.2f", totalCr)
                });

                if (totalDr >= totalCr) {
                    double balance = totalDr - totalCr;
                    model.addRow(new Object[]{
                        "", "BALANCE", String.format("%.2f", balance),
                        "", "", ""
                    });
                } else {
                    double balance = totalCr - totalDr;
                    model.addRow(new Object[]{
                        "", "BALANCE", "",
                        "", "", String.format("%.2f", balance)
                    });
                }

                JTable table = new JTable(model);
                table.setRowHeight(20);
                table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
                
                DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
                rightAlign.setHorizontalAlignment(JLabel.RIGHT);
                table.getColumnModel().getColumn(2).setCellRenderer(rightAlign);
                table.getColumnModel().getColumn(5).setCellRenderer(rightAlign);

                JScrollPane tableScroll = new JScrollPane(table);
                tableScroll.setPreferredSize(new Dimension(880, 100));
                accountPanel.add(tableScroll, BorderLayout.CENTER);

                containerPanel.add(accountPanel);
                containerPanel.add(Box.createVerticalStrut(10));
            }
        }

        containerPanel.revalidate();
        containerPanel.repaint();
    }

    private static class LedgerItem {
        String date, explanation;
        double debit, credit;
        public LedgerItem(String date, String explanation, double debit, double credit) {
            this.date = date;
            this.explanation = explanation;
            this.debit = debit;
            this.credit = credit;
        }
    }
}