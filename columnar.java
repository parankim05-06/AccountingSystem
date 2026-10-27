package home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class columnar extends JPanel {
    private journal journalRef;
    private String username;
    private DefaultTableModel tableModel;

    private final Color WARM_CREAM = new Color(248, 246, 242);
    private final Color SOFT_CARD = new Color(252, 250, 248);
    private final Color WARM_BEIGE = new Color(230, 225, 218);
    private final Color DEEP_MOCHA = new Color(92, 64, 51);
    private final Color TEXT_DARK = new Color(60, 50, 45);

    public columnar(journal journalRef, String username) {
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
        ledgerTab.setBackground(WARM_BEIGE);
        ledgerTab.setForeground(TEXT_DARK);
        ledgerTab.setFont(new Font("SansSerif", Font.BOLD, 12));
        ledgerTab.setFocusPainted(false);
        ledgerTab.addActionListener(e -> {
            journalRef.loadJournalsFromFile();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
            JFrame frame = new JFrame("Accounting System Workspace");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new ledger(journalRef, username));
            frame.setVisible(true);
        });

        JButton columnarTab = new JButton("12 COLUMNAR SHEET");
        columnarTab.setBackground(DEEP_MOCHA);
        columnarTab.setForeground(Color.WHITE);
        columnarTab.setFont(new Font("SansSerif", Font.BOLD, 12));
        columnarTab.setFocusPainted(false);

        navBar.add(journalTab);
        navBar.add(ledgerTab);
        navBar.add(columnarTab);

        // --- HEADER WITH REFRESH ---
        JLabel moduleTitle = new JLabel("12 COLUMNAR SHEET");
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
            journalRef.loadJournalsFromFile();
            tableModel.setRowCount(0);
            populateWorksheetData();
            JOptionPane.showMessageDialog(this, "12 Columnar Sheet refreshed!");
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

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(WARM_CREAM);
        topHeader.add(moduleTitle, BorderLayout.WEST);
        topHeader.add(headerRight, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setBackground(WARM_CREAM);
        topContainer.add(navBar, BorderLayout.NORTH);
        topContainer.add(topHeader, BorderLayout.SOUTH);

        // --- TABLE PANEL ---
        JPanel mainCard = new JPanel(new BorderLayout());
        mainCard.setBackground(SOFT_CARD);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(WARM_BEIGE, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        String[] columns = {
            "Account Title", 
            "Trial Balance Dr", "Trial Balance Cr", 
            "Adjustments Dr", "Adjustments Cr", 
            "Adjusted Trial Balance Dr", "Adjusted Trial Balance Cr", 
            "Income Statement Dr", "Income Statement Cr", 
            "Owner's Equity Dr", "Owner's Equity Cr", 
            "Balance Sheet Dr", "Balance Sheet Cr"
        };

        tableModel = new DefaultTableModel(columns, 0);
        populateWorksheetData();

        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        for (int i = 1; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightAlign);
            table.getColumnModel().getColumn(i).setPreferredWidth(120);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(160);

        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainCard.add(scrollPane, BorderLayout.CENTER);

        add(topContainer, BorderLayout.NORTH);
        add(mainCard, BorderLayout.CENTER);
    }

    private void populateWorksheetData() {
        Map<String, Double> trialDr = new HashMap<>();
        Map<String, Double> trialCr = new HashMap<>();
        Map<String, String> accountTypes = new HashMap<>();

        for (journal.JournalRecord rec : journalRef.allEntries) {
            for (journal.EntryLine line : rec.lines) {
                if (line.account == null || line.account.trim().isEmpty()) continue;
                String accTitle = line.account.trim().toUpperCase();
                String accType = line.type != null ? line.type.toUpperCase() : "ASSET";
                
                accountTypes.put(accTitle, accType);
                if (line.debit > 0) {
                    trialDr.put(accTitle, trialDr.getOrDefault(accTitle, 0.0) + line.debit);
                }
                if (line.credit > 0) {
                    trialCr.put(accTitle, trialCr.getOrDefault(accTitle, 0.0) + line.credit);
                }
            }
        }

        List<String> accounts = new ArrayList<>(accountTypes.keySet());
        Collections.sort(accounts);

        double tTbDr = 0, tTbCr = 0;
        double tAdjDr = 0, tAdjCr = 0;
        double tAtbDr = 0, tAtbCr = 0;
        double tIsDr = 0, tIsCr = 0;
        double tOeDr = 0, tOeCr = 0;
        double tBsDr = 0, tBsCr = 0;

        for (String acc : accounts) {
            double dr = trialDr.getOrDefault(acc, 0.0);
            double cr = trialCr.getOrDefault(acc, 0.0);
            String type = accountTypes.get(acc);

            tTbDr += dr;
            tTbCr += cr;

            double atbDr = 0, atbCr = 0;
            double netBal = dr - cr;
            if (netBal >= 0) {
                atbDr = netBal;
            } else {
                atbCr = Math.abs(netBal);
            }

            tAtbDr += atbDr;
            tAtbCr += atbCr;

            double isDr = 0, isCr = 0;
            double oeDr = 0, oeCr = 0;
            double bsDr = 0, bsCr = 0;

            if (type.equals("INCOME")) {
                isCr = atbCr > 0 ? atbCr : atbDr;
                tIsCr += isCr;
            } else if (type.equals("EXPENSE")) {
                isDr = atbDr > 0 ? atbDr : atbCr;
                tIsDr += isDr;
            } else if (type.equals("EQUITY")) {
                oeCr = atbCr > 0 ? atbCr : atbDr;
                tOeCr += oeCr;
            } else {
                if (type.equals("ASSET")) {
                    bsDr = atbDr;
                    tBsDr += bsDr;
                } else if (type.equals("LIABILITY")) {
                    bsCr = atbCr;
                    tBsCr += bsCr;
                }
            }

            tableModel.addRow(new Object[]{
                acc,
                dr > 0 ? String.format("%.2f", dr) : "",
                cr > 0 ? String.format("%.2f", cr) : "",
                "", "",
                atbDr > 0 ? String.format("%.2f", atbDr) : "",
                atbCr > 0 ? String.format("%.2f", atbCr) : "",
                isDr > 0 ? String.format("%.2f", isDr) : "",
                isCr > 0 ? String.format("%.2f", isCr) : "",
                oeDr > 0 ? String.format("%.2f", oeDr) : "",
                oeCr > 0 ? String.format("%.2f", oeCr) : "",
                bsDr > 0 ? String.format("%.2f", bsDr) : "",
                bsCr > 0 ? String.format("%.2f", bsCr) : ""
            });
        }

        tableModel.addRow(new Object[]{
            "TOTAL",
            String.format("%.2f", tTbDr), String.format("%.2f", tTbCr),
            String.format("%.2f", tAdjDr), String.format("%.2f", tAdjCr),
            String.format("%.2f", tAtbDr), String.format("%.2f", tAtbCr),
            String.format("%.2f", tIsDr), String.format("%.2f", tIsCr),
            String.format("%.2f", tOeDr), String.format("%.2f", tOeCr),
            String.format("%.2f", tBsDr), String.format("%.2f", tBsCr)
        });
    }
}