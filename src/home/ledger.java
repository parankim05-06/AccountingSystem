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

/** Ledger screen matching the UI design of Image #10. */
public class ledger extends JPanel {

    private journal journalRef;
    private String username;
    private JPanel containerPanel;

    private final Color SIDEBAR_BG = new Color(20, 20, 20);
    private final Color MAIN_BG = new Color(245, 245, 245);
    private final Color CARD_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(220, 220, 220);
    private final Color TEXT_DARK = new Color(40, 40, 40);
    private final Color TEXT_MUTED = new Color(120, 120, 120);

    public ledger(journal journalRef, String username) {
        this.journalRef = journalRef;
        this.username = username;

        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        // Sidebar Navigation
        add(createSidebar(), BorderLayout.WEST);

        // Main Content Area
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(MAIN_BG);
        contentPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Top Header
        contentPanel.add(createTopHeader(), BorderLayout.NORTH);

        // Scrollable Ledger Container
        containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(MAIN_BG);

        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(MAIN_BG);

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        buildLedgerTables();
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
        navLinks.add(createNavLink("Journal entry", false));
        navLinks.add(Box.createVerticalStrut(10));
        navLinks.add(createNavLink("Ledger", true));
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
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) window.dispose();
            } else if (text.equals("Journal entry")) {
                journalRef.loadJournalsFromFile();
                journalRef.refreshTable();
                switchView(journalRef);
            } else if (text.equals("12-column sheet")) {
                journalRef.loadJournalsFromFile();
                switchView(new columnar(journalRef, username));
            }
        });

        return btn;
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAIN_BG);

        JLabel title = new JLabel("Ledger");
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
            journalRef.loadJournalsFromFile();
            buildLedgerTables();
        });

        header.add(title, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);

        return header;
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

    private void buildLedgerTables() {
        containerPanel.removeAll();

        Map<String, List<LedgerItem>> accountEntries = new HashMap<>();
        for (journal.JournalRecord rec : journalRef.allEntries) {
            if (rec.isVoided || rec.isAdjusting) continue;

            for (journal.EntryLine line : rec.lines) {
                if (line.account == null || line.account.trim().isEmpty()) continue;
                String accName = line.account.trim().toUpperCase();
                accountEntries.putIfAbsent(accName, new ArrayList<>());
                accountEntries.get(accName).add(new LedgerItem(rec.date, rec.description, line.debit, line.credit));
            }
        }

        if (accountEntries.isEmpty()) {
            JLabel noData = new JLabel("No ledger entries found.");
            noData.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noData.setForeground(TEXT_MUTED);
            containerPanel.add(noData);
        } else {
            for (Map.Entry<String, List<LedgerItem>> entry : accountEntries.entrySet()) {
                String accountTitle = entry.getKey();
                List<LedgerItem> items = entry.getValue();

                List<LedgerItem> debits = new ArrayList<>();
                List<LedgerItem> credits = new ArrayList<>();
                double totalDr = 0, totalCr = 0;

                for (LedgerItem item : items) {
                    if (item.debit > 0) { debits.add(item); totalDr += item.debit; }
                    if (item.credit > 0) { credits.add(item); totalCr += item.credit; }
                }

                JPanel accountPanel = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
                accountPanel.setLayout(new BorderLayout(10, 15));
                accountPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

                JLabel titleLabel = new JLabel("Account title: " + accountTitle);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
                titleLabel.setForeground(TEXT_DARK);
                accountPanel.add(titleLabel, BorderLayout.NORTH);

                String[] cols = {"Date", "Explanation", "Debit", "Date", "Explanation", "Credit"};
                DefaultTableModel model = new DefaultTableModel(cols, 0) {
                    @Override public boolean isCellEditable(int row, int col) { return false; }
                };

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

                model.addRow(new Object[]{"", "TOTAL", String.format("%.2f", totalDr), "", "TOTAL", String.format("%.2f", totalCr)});
                double balance = Math.abs(totalDr - totalCr);
                if (totalDr >= totalCr) {
                    model.addRow(new Object[]{"", "BALANCE", String.format("%.2f", balance), "", "", ""});
                } else {
                    model.addRow(new Object[]{"", "", "", "", "BALANCE", String.format("%.2f", balance)});
                }

                JTable table = new JTable(model);
                table.setRowHeight(38);
                table.setShowGrid(true);
                table.setGridColor(new Color(230, 230, 230));
                table.setIntercellSpacing(new Dimension(1, 1));
                table.setFocusable(false);

                // Table Header Custom Renderer
                table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                        JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                        lbl.setOpaque(true);
                        lbl.setBackground(Color.WHITE);
                        lbl.setForeground(TEXT_MUTED);
                        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

                        if (col == 2 || col == 5) {
                            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                        } else {
                            lbl.setHorizontalAlignment(SwingConstants.LEFT);
                        }

                        Color rightColor = (col == 2) ? new Color(180, 180, 180) : (col == 5 ? new Color(0, 0, 0, 0) : new Color(230, 230, 230));
                        lbl.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(0, 0, 1, col == 5 ? 0 : 1, rightColor),
                                new EmptyBorder(8, 12, 8, col == 2 ? 16 : 12)
                        ));
                        return lbl;
                    }
                });

                // Table Data Custom Renderer (Visible grid lines, vertical separator down middle, bold TOTAL/BALANCE)
                table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                        JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                        c.setOpaque(true);
                        c.setBackground(isSelected ? tbl.getSelectionBackground() : Color.WHITE);

                        if (col == 2 || col == 5) {
                            c.setHorizontalAlignment(SwingConstants.RIGHT);
                        } else {
                            c.setHorizontalAlignment(SwingConstants.LEFT);
                        }

                        int totalRow = model.getRowCount() - 2;
                        int balanceRow = model.getRowCount() - 1;

                        if (row == totalRow || row == balanceRow) {
                            c.setFont(new Font("Segoe UI", Font.BOLD, 14));
                            c.setForeground(Color.BLACK);
                        } else {
                            c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                            c.setForeground(TEXT_DARK);
                        }

                        Color rightColor = (col == 2) ? new Color(180, 180, 180) : (col == 5 ? Color.WHITE : new Color(230, 230, 230));
                        int topWidth = (row == totalRow) ? 1 : 0;
                        Color topColor = new Color(180, 180, 180);
                        Color bottomColor = (row == totalRow || row == balanceRow) ? new Color(180, 180, 180) : new Color(230, 230, 230);

                        c.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(topWidth, 0, 1, col == 5 ? 0 : 1, rightColor),
                                BorderFactory.createCompoundBorder(
                                        BorderFactory.createMatteBorder(topWidth, 0, 1, 0, topWidth > 0 ? topColor : bottomColor),
                                        new EmptyBorder(8, 12, 8, col == 2 ? 16 : 12)
                                )
                        ));

                        return c;
                    }
                });

                JScrollPane tableScroll = new JScrollPane(table);
                tableScroll.setBorder(null);
                tableScroll.getViewport().setBackground(Color.WHITE);

                int calcHeight = (model.getRowCount() + 1) * 38 + 20;
                tableScroll.setPreferredSize(new Dimension(0, calcHeight));

                JPanel tableWrapper = new RoundedPanel(12, Color.WHITE, BORDER_COLOR);
                tableWrapper.setLayout(new BorderLayout());
                tableWrapper.setBorder(new EmptyBorder(2, 2, 2, 2));
                tableWrapper.add(tableScroll, BorderLayout.CENTER);

                accountPanel.add(tableWrapper, BorderLayout.CENTER);
                accountPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, calcHeight + 80));

                containerPanel.add(accountPanel);
                containerPanel.add(Box.createVerticalStrut(25));
            }
        }
        containerPanel.revalidate();
        containerPanel.repaint();
    }

    private static class LedgerItem {
        String date, explanation;
        double debit, credit;
        public LedgerItem(String d, String e, double dr, double cr) {
            date = d; explanation = e; debit = dr; credit = cr;
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
