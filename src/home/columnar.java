package home;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class columnar extends JPanel {

    private journal journalRef;
    private String username;
    private DefaultTableModel tableModel;
    private DefaultTableModel accountTitleModel;
    private JTable table;
    private JTable accountTable;
    private int accountRowCount = 0;
    private boolean rebuildingWorksheet = false;

    private JLabel adjustmentStatusLabel =
            new PillLabel("ADJUSTMENTS VERIFICATION: NOT CHECKED");

    private JLabel balanceStatusLabel =
            new PillLabel("BALANCE SHEET VERIFICATION: NOT CHECKED");

    private final Color SIDEBAR_BG =
            new Color(20, 20, 20);
    private final Color MAIN_BG =
            new Color(245, 245, 245);
    private final Color CARD_BG =
            new Color(255, 255, 255);
    private final Color BORDER_COLOR =
            new Color(220, 220, 220);
    private final Color TEXT_DARK =
            new Color(40, 40, 40);

    private Font appFont(int style, int size) {
        String[] families = {"Segoe UI", "Myanmar Text", "SansSerif"};
        for (String family : families) {
            Font font = new Font(family, style, size);
            if (font.getFamily().equalsIgnoreCase(family) || family.equalsIgnoreCase("SansSerif")) {
                return font;
            }
        }
        return new Font(Font.SANS_SERIF, style, size);
    }

    public columnar(journal journalRef, String username) {
        this.journalRef = journalRef;
        this.username = username;

        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        rebuildWorksheet();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Logo Section
        JLabel logo = new JLabel("📋 ENTRI");
        logo.setFont(appFont(Font.BOLD, 20));
        logo.setForeground(Color.WHITE);
        logo.setBorder(new EmptyBorder(0, 0, 40, 0));

        // Nav Links
        JPanel navLinks = new JPanel();
        navLinks.setLayout(new BoxLayout(navLinks, BoxLayout.Y_AXIS));
        navLinks.setBackground(SIDEBAR_BG);

        navLinks.add(createSidebarLink("Dashboard", false));
        navLinks.add(Box.createVerticalStrut(10));
        navLinks.add(createSidebarLink("Journal entry", false));
        navLinks.add(Box.createVerticalStrut(10));
        navLinks.add(createSidebarLink("Ledger", false));
        navLinks.add(Box.createVerticalStrut(10));
        navLinks.add(createSidebarLink("12-column sheet", true));

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(SIDEBAR_BG);
        topSection.add(logo, BorderLayout.NORTH);
        topSection.add(navLinks, BorderLayout.CENTER);

        // Bottom Section
        JPanel bottomSection = new JPanel();
        bottomSection.setLayout(new BoxLayout(bottomSection, BoxLayout.Y_AXIS));
        bottomSection.setBackground(SIDEBAR_BG);

        JLabel userLabel = new JLabel("Signed in as " + username);
        userLabel.setFont(appFont(Font.PLAIN, 13));
        userLabel.setForeground(new Color(180, 180, 180));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JButton logoutBtn = new JButton("Log out");
        logoutBtn.setFont(appFont(Font.BOLD, 14));
        logoutBtn.setBackground(Color.WHITE);
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setMaximumSize(new Dimension(190, 40));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> {
            new ui.LoginFrame().setVisible(true);
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
        });

        bottomSection.add(userLabel);
        bottomSection.add(logoutBtn);

        sidebar.add(topSection, BorderLayout.NORTH);
        sidebar.add(bottomSection, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createSidebarLink(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(appFont(Font.BOLD, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(190, 45));
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
            } else if (text.equals("Ledger")) {
                journalRef.loadJournalsFromFile();
                switchView(new ledger(journalRef, username));
            }
        });

        return btn;
    }

    private JPanel createMainContent() {
        JPanel mainPane = new JPanel(new BorderLayout(20, 14));
        mainPane.setBackground(MAIN_BG);
        mainPane.setBorder(new EmptyBorder(20, 26, 20, 26));

        // Top Header
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(MAIN_BG);

        JLabel moduleTitle = new JLabel("12-column sheet");
        moduleTitle.setFont(appFont(Font.BOLD, 28));
        moduleTitle.setForeground(TEXT_DARK);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setBackground(MAIN_BG);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setForeground(TEXT_DARK);
        refreshBtn.setFont(appFont(Font.BOLD, 14));
        refreshBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> {
            journalRef.loadJournalsFromFile();
            rebuildWorksheet();
            JOptionPane.showMessageDialog(this, "12-column sheet refreshed!");
        });

        JButton editAdjustmentBtn = new JButton("Edit adjustment");
        editAdjustmentBtn.setBackground(Color.BLACK);
        editAdjustmentBtn.setForeground(Color.WHITE);
        editAdjustmentBtn.setFont(appFont(Font.BOLD, 14));
        editAdjustmentBtn.setBorder(new EmptyBorder(8, 20, 8, 20));
        editAdjustmentBtn.setFocusPainted(false);
        editAdjustmentBtn.addActionListener(e -> editAdjustment());

        headerRight.add(refreshBtn);
        headerRight.add(editAdjustmentBtn);

        topHeader.add(moduleTitle, BorderLayout.WEST);
        topHeader.add(headerRight, BorderLayout.EAST);

        // Sheet Card with Frozen Table inside a RoundedPanel
        RoundedPanel sheetCard = new RoundedPanel(16, CARD_BG, BORDER_COLOR);
        sheetCard.setLayout(new BorderLayout());
        sheetCard.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Define the 12 scrollable financial columns
        String[] columns = {
            "TB Debit", "TB Credit",
            "Adj Debit", "Adj Credit",
            "ATB Debit", "ATB Credit",
            "IS Debit", "IS Credit",
            "OE Debit", "OE Credit",
            "BS Debit", "BS Credit"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        // Create the main scrollable data table (12 columns)
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(32);
        table.setRowMargin(0);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(240, 244, 248));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);
        table.setFont(appFont(Font.PLAIN, 13));

        // Configure column widths for the 12 data columns
        int[] colWidths = {110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110, 110};
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        // Right-align all data columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        rightRenderer.setFont(appFont(Font.PLAIN, 13));
        rightRenderer.setBorder(new EmptyBorder(0, 8, 0, 12));
        for (int i = 0; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        table.setTableHeader(new ColumnarHeader(table));

        // Create frozen column table (Account title)
        accountTitleModel = new DefaultTableModel(new Object[]{"Account title"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        accountTable = new JTable(accountTitleModel);
        accountTable.setRowHeight(32);
        accountTable.setRowMargin(0);
        accountTable.setIntercellSpacing(new Dimension(0, 0));
        accountTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        accountTable.setShowGrid(true);
        accountTable.setGridColor(new Color(230, 230, 230));
        accountTable.setEnabled(true);
        accountTable.setSelectionBackground(new Color(240, 244, 248));
        accountTable.setSelectionForeground(TEXT_DARK);
        accountTable.setBorder(null);
        accountTable.setFont(appFont(Font.PLAIN, 13));
        accountTable.setFillsViewportHeight(true);
        accountTable.getTableHeader().setVisible(false);
        accountTable.getColumnModel().getColumn(0).setPreferredWidth(220);

        DefaultTableCellRenderer accountRenderer = new DefaultTableCellRenderer();
        accountRenderer.setHorizontalAlignment(JLabel.LEFT);
        accountRenderer.setFont(appFont(Font.PLAIN, 13));
        accountRenderer.setBorder(new EmptyBorder(0, 14, 0, 8));
        accountTable.getColumnModel().getColumn(0).setCellRenderer(accountRenderer);

        // Frozen table corner header matching the 2-row layout of the scrollable header
        JPanel frozenHeader = new JPanel(new GridLayout(2, 1, 0, 0));
        frozenHeader.setBackground(CARD_BG);
        frozenHeader.setPreferredSize(new Dimension(220, 44));

        JLabel frozenHeaderTop = new JLabel("", SwingConstants.LEFT);
        frozenHeaderTop.setOpaque(true);
        frozenHeaderTop.setBackground(CARD_BG);
        frozenHeaderTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(210, 210, 210)));

        JLabel frozenHeaderBottom = new JLabel("Account title", SwingConstants.LEFT);
        frozenHeaderBottom.setOpaque(true);
        frozenHeaderBottom.setBackground(CARD_BG);
        frozenHeaderBottom.setFont(appFont(Font.BOLD, 12));
        frozenHeaderBottom.setForeground(TEXT_DARK);
        frozenHeaderBottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(210, 210, 210)),
            new EmptyBorder(0, 14, 0, 0)
        ));

        frozenHeader.add(frozenHeaderTop);
        frozenHeader.add(frozenHeaderBottom);

        // Create the frozen table pane
        FrozenTablePane frozenPane = new FrozenTablePane(accountTable, table);
        frozenPane.setCornerComponent(frozenHeader);
        frozenPane.setBorder(BorderFactory.createEmptyBorder());

        // Verification panel
        JPanel verificationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        verificationPanel.setBackground(MAIN_BG);
        verificationPanel.setBorder(new EmptyBorder(16, 0, 0, 0));
        verificationPanel.add(adjustmentStatusLabel);
        verificationPanel.add(balanceStatusLabel);

        // Add frozen pane directly to sheetCard (no separate group header panel)
        sheetCard.add(frozenPane, BorderLayout.CENTER);

        mainPane.add(topHeader, BorderLayout.NORTH);
        mainPane.add(sheetCard, BorderLayout.CENTER);
        mainPane.add(verificationPanel, BorderLayout.SOUTH);

        return mainPane;
    }

    public void switchView(JPanel panel) {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            frame.getContentPane().removeAll();
            frame.getContentPane().add(panel);
            frame.revalidate();
            frame.repaint();
        } else {
            JFrame frame = new JFrame("Accounting System Workspace");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1100, 700);
            frame.setLocationRelativeTo(null);
            frame.add(panel);
            frame.setVisible(true);
            Window win = SwingUtilities.getWindowAncestor(this);
            if (win != null) win.dispose();
        }
    }

    private void rebuildWorksheet() {
        if (tableModel == null) return;
        rebuildingWorksheet = true;
        try {
            tableModel.setRowCount(0);
            accountTitleModel.setRowCount(0);
            populateWorksheetData();
        } finally {
            rebuildingWorksheet = false;
        }
    }

    private class ColumnarHeader extends JTableHeader {
    private final String[] groupNames = {
        "Trial balance",
        "Adjustments",
        "Adjusted trial balance",
        "Income statement",
        "Owner's equity",
        "Balance sheet"
    };

    private final String[] subColumns = {
        "Debit", "Credit",
        "Debit", "Credit",
        "Debit", "Credit",
        "Debit", "Credit",
        "Debit", "Credit",
        "Debit", "Credit"
    };

    public ColumnarHeader(JTable table) {
        super(table.getColumnModel());
        setReorderingAllowed(false);
        setResizingAllowed(false);
        setOpaque(true);
        setBackground(new Color(245, 245, 245));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 44);
    }

    private void drawCenteredText(
            Graphics2D g2,
            String text,
            int x,
            int y,
            int width,
            int height
    ) {
        FontMetrics fm = g2.getFontMetrics();

        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();

        g2.drawString(text, textX, textY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            int topHeight = 22;
            int bottomHeight = 22;

            int x = 0;

            // TOP ROW: GROUP HEADINGS
            g2.setFont(appFont(Font.BOLD, 12));

            for (int group = 0; group < groupNames.length; group++) {
                int firstColumn = group * 2;

                int groupWidth =
                    getColumnModel().getColumn(firstColumn).getWidth()
                    + getColumnModel().getColumn(firstColumn + 1).getWidth();

                g2.setColor(new Color(245, 245, 245));
                g2.fillRect(x, 0, groupWidth, topHeight);

                g2.setColor(new Color(80, 80, 80));

                drawCenteredText(
                    g2,
                    groupNames[group],
                    x,
                    0,
                    groupWidth,
                    topHeight
                );

                g2.setColor(new Color(210, 210, 210));
                g2.drawRect(x, 0, groupWidth, topHeight);

                x += groupWidth;
            }

            // BOTTOM ROW: DEBIT AND CREDIT
            x = 0;

            g2.setFont(appFont(Font.BOLD, 11));

            for (int column = 0; column < subColumns.length; column++) {
                int columnWidth =
                    getColumnModel().getColumn(column).getWidth();

                g2.setColor(new Color(245, 245, 245));
                g2.fillRect(x, topHeight, columnWidth, bottomHeight);

                g2.setColor(new Color(80, 80, 80));

                drawCenteredText(
                    g2,
                    subColumns[column],
                    x,
                    topHeight,
                    columnWidth,
                    bottomHeight
                );

                g2.setColor(new Color(210, 210, 210));
                g2.drawRect(x, topHeight, columnWidth, bottomHeight);

                x += columnWidth;
            }

        } finally {
            g2.dispose();
        }
    }
}

    private String money(double amount) {
        if (Math.abs(amount) < 0.005) {
            return "";
        }
        return String.format("%.2f", amount);
    }

    private String normalizeAccountName(String account) {
        return account == null ? "" : account.trim().toUpperCase();
    }

    private boolean isDrawingAccount(String account) {
        return normalizeAccountName(account).contains("DRAWING");
    }

    private void updateVerificationLabel(JLabel label, String title, boolean balanced) {
        label.setText(title + ": " + (balanced ? "BALANCED" : "NOT BALANCED"));
        label.setForeground(Color.WHITE);
    }

    private void addWorksheetRow(String title, Object[] values) {
        accountTitleModel.addRow(new Object[]{title});
        tableModel.addRow(values);
    }

    private void populateWorksheetData() {
        accountRowCount = 0;
        tableModel.setRowCount(0);
        accountTitleModel.setRowCount(0);

        Map<String, Double> trialDr = new HashMap<>();
        Map<String, Double> trialCr = new HashMap<>();
        Map<String, Double> adjustmentDr = new HashMap<>();
        Map<String, Double> adjustmentCr = new HashMap<>();
        Map<String, String> accountTypes = new HashMap<>();

        for (journal.JournalRecord rec : journalRef.allEntries) {
            if (rec.isVoided) continue;
            for (journal.EntryLine line : rec.lines) {
                if (line.account == null || line.account.trim().isEmpty()) continue;

                String account = normalizeAccountName(line.account);
                String type = line.type != null ? line.type.trim().toUpperCase() : "ASSET";
                accountTypes.put(account, type);

                if (rec.isAdjusting) {
                    if (line.debit > 0) {
                        adjustmentDr.put(account, adjustmentDr.getOrDefault(account, 0.0) + line.debit);
                    }
                    if (line.credit > 0) {
                        adjustmentCr.put(account, adjustmentCr.getOrDefault(account, 0.0) + line.credit);
                    }
                } else {
                    if (line.debit > 0) {
                        trialDr.put(account, trialDr.getOrDefault(account, 0.0) + line.debit);
                    }
                    if (line.credit > 0) {
                        trialCr.put(account, trialCr.getOrDefault(account, 0.0) + line.credit);
                    }
                }
            }
        }

        List<String> accounts = new ArrayList<>(accountTypes.keySet());
        Map<String, Integer> classificationOrder = new HashMap<>();
        classificationOrder.put("ASSET", 1);
        classificationOrder.put("LIABILITY", 2);
        classificationOrder.put("EQUITY", 3);
        classificationOrder.put("CAPITAL", 3);
        classificationOrder.put("REVENUE", 4);
        classificationOrder.put("INCOME", 4);
        classificationOrder.put("EXPENSE", 5);

        accounts.sort((a, b) -> {
            int orderA = classificationOrder.getOrDefault(accountTypes.getOrDefault(a, ""), 99);
            int orderB = classificationOrder.getOrDefault(accountTypes.getOrDefault(b, ""), 99);
            if (orderA != orderB) return Integer.compare(orderA, orderB);
            return a.compareToIgnoreCase(b);
        });

        double totalTbDr = 0, totalTbCr = 0;
        double totalAdjDr = 0, totalAdjCr = 0;
        double totalAtbDr = 0, totalAtbCr = 0;
        double totalIsDr = 0, totalIsCr = 0;
        double totalOeDr = 0, totalOeCr = 0;
        double totalBsDr = 0, totalBsCr = 0;

        for (String account : accounts) {
            double originalDr = trialDr.getOrDefault(account, 0.0);
            double originalCr = trialCr.getOrDefault(account, 0.0);
            String type = accountTypes.get(account);

            double trialNet = originalDr - originalCr;
            double tbDr = Math.max(trialNet, 0);
            double tbCr = Math.max(-trialNet, 0);
            totalTbDr += tbDr;
            totalTbCr += tbCr;

            double adjDr = adjustmentDr.getOrDefault(account, 0.0);
            double adjCr = adjustmentCr.getOrDefault(account, 0.0);
            totalAdjDr += adjDr;
            totalAdjCr += adjCr;

            double adjustedNet = trialNet + adjDr - adjCr;
            double atbDr = Math.max(adjustedNet, 0);
            double atbCr = Math.max(-adjustedNet, 0);
            totalAtbDr += atbDr;
            totalAtbCr += atbCr;

            double isDr = 0, isCr = 0;
            double oeDr = 0, oeCr = 0;
            double bsDr = 0, bsCr = 0;

            // INCOME STATEMENT: preserve the account's actual adjusted
            // debit or credit balance instead of duplicating one side.
            if (type.equals("INCOME") || type.equals("REVENUE")) {
                isDr = atbDr;
                isCr = atbCr;
            } else if (type.equals("EXPENSE")) {
                isDr = atbDr;
                isCr = atbCr;
            }
            // OWNER'S EQUITY: drawings normally carry a debit balance;
            // capital/equity accounts normally carry a credit balance.
            else if (isDrawingAccount(account)) {
                oeDr = atbDr;
                oeCr = atbCr;
            } else if (type.equals("EQUITY") || type.equals("CAPITAL")) {
                oeDr = atbDr;
                oeCr = atbCr;
            }
            // BALANCE SHEET: assets and liabilities retain their actual
            // adjusted debit/credit balances.
            else if (type.equals("ASSET") || type.equals("LIABILITY")) {
                bsDr = atbDr;
                bsCr = atbCr;
            }

            totalIsDr += isDr;
            totalIsCr += isCr;
            totalOeDr += oeDr;
            totalOeCr += oeCr;
            totalBsDr += bsDr;
            totalBsCr += bsCr;

            addWorksheetRow(account, new Object[]{
                    money(tbDr), money(tbCr),
                    money(adjDr), money(adjCr),
                    money(atbDr), money(atbCr),
                    money(isDr), money(isCr),
                    money(oeDr), money(oeCr),
                    money(bsDr), money(bsCr)
            });

            accountRowCount++;
        }

        double netResult = totalIsCr - totalIsDr;
        double netIncome = Math.max(netResult, 0);
        double netLoss = Math.max(-netResult, 0);
        double endingCapital = totalOeCr - totalOeDr + netResult;

        addWorksheetRow("TOTAL", new Object[]{
                money(totalTbDr), money(totalTbCr),
                money(totalAdjDr), money(totalAdjCr),
                money(totalAtbDr), money(totalAtbCr),
                money(totalIsDr), money(totalIsCr),
                money(totalOeDr), money(totalOeCr),
                money(totalBsDr), money(totalBsCr)
        });

        addWorksheetRow("NET INCOME", new Object[]{
                "", "", "", "", "", "",
                money(netIncome), money(netLoss),
                "", "", "", ""
        });

        addWorksheetRow("ENDING OWNER'S CAPITAL", new Object[]{
                "", "", "", "", "", "",
                "", "",
                money(endingCapital), "",
                "", ""
        });

        double grandIsDr = totalIsDr + netIncome;
        double grandIsCr = totalIsCr + netLoss;
        double grandBsDr = totalBsDr;
        double grandBsCr = totalBsCr + endingCapital;

        updateVerificationLabel(adjustmentStatusLabel, "ADJUSTMENTS VERIFICATION", isAdjustmentBalanced(totalAdjDr, totalAdjCr));
        updateVerificationLabel(balanceStatusLabel, "BALANCE SHEET VERIFICATION", isBalanceSheetBalanced(grandBsDr, grandBsCr));

        addWorksheetRow("GRAND TOTAL", new Object[]{
                "", "", "", "", "", "",
                money(grandIsDr), money(grandIsCr),
                "", "",
                money(grandBsDr), money(grandBsCr)
        });
    }

    private boolean isAdjustmentBalanced(double adjustmentDebit, double adjustmentCredit) {
        return Math.abs(adjustmentDebit - adjustmentCredit) < 0.01;
    }

    private boolean isBalanceSheetBalanced(double balanceSheetDebit, double balanceSheetCredit) {
        return Math.abs(balanceSheetDebit - balanceSheetCredit) < 0.01;
    }

    private void editAdjustment() {
        List<journal.JournalRecord> adjustments = new ArrayList<>();
        for (journal.JournalRecord record : journalRef.allEntries) {
            if (record.isAdjusting && !record.isVoided) adjustments.add(record);
        }

        if (adjustments.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no active Adjusting Entries to edit.", "No Adjustments", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = new String[adjustments.size()];
        for (int i = 0; i < adjustments.size(); i++) {
            journal.JournalRecord record = adjustments.get(i);
            options[i] = "ID: " + record.id + " | " + record.date + " | " + record.description;
        }

        JComboBox<String> adjustmentSelector = new JComboBox<>(options);
        int selection = JOptionPane.showConfirmDialog(this, adjustmentSelector, "SELECT ADJUSTING ENTRY", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (selection != JOptionPane.OK_OPTION) return;

        int selectedIndex = adjustmentSelector.getSelectedIndex();
        if (selectedIndex < 0) return;

        journal.JournalRecord record = adjustments.get(selectedIndex);
        journal.EntryLine debitLine = null;
        journal.EntryLine creditLine = null;

        for (journal.EntryLine line : record.lines) {
            if (line.debit > 0) debitLine = line;
            if (line.credit > 0) creditLine = line;
        }

        if (debitLine == null || creditLine == null) {
            JOptionPane.showMessageDialog(this, "This adjustment must contain one debit and one credit line.", "Cannot Edit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatePicker datePicker = new DatePicker();
datePicker.setDate(record.date);
        JTextField descriptionField = new JTextField(record.description);
        JTextField debitAccountField = new JTextField(debitLine.account);
        JTextField creditAccountField = new JTextField(creditLine.account);

        String[] types = {"ASSET", "EQUITY", "EXPENSE", "LIABILITY", "REVENUE"};
        JComboBox<String> debitType = new JComboBox<>(types);
        JComboBox<String> creditType = new JComboBox<>(types);
        debitType.setSelectedItem(debitLine.type);
        creditType.setSelectedItem(creditLine.type);

        JTextField amountField = new JTextField(String.valueOf(debitLine.debit));

        JPanel panel = new JPanel(new GridLayout(7, 2, 8, 8));
        panel.add(new JLabel("Date (DD-MM-YYYY):")); panel.add(datePicker);
        panel.add(new JLabel("Description:")); panel.add(descriptionField);
        panel.add(new JLabel("Debit Account:")); panel.add(debitAccountField);
        panel.add(new JLabel("Debit Account Type:")); panel.add(debitType);
        panel.add(new JLabel("Credit Account:")); panel.add(creditAccountField);
        panel.add(new JLabel("Credit Account Type:")); panel.add(creditType);
        panel.add(new JLabel("Amount:")); panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "EDIT ADJUSTING ENTRY ID " + record.id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String date = datePicker.getDateString().trim();
            String description = descriptionField.getText().trim();
            String debitAccount = debitAccountField.getText().trim();
            String creditAccount = creditAccountField.getText().trim();
            double amount = Double.parseDouble(amountField.getText().trim());

            if (date.isEmpty() || description.isEmpty() || debitAccount.isEmpty() || creditAccount.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please complete all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<journal.EntryLine> updatedLines = new ArrayList<>();
            updatedLines.add(new journal.EntryLine(debitAccount, (String) debitType.getSelectedItem(), amount, 0.0));
            updatedLines.add(new journal.EntryLine(creditAccount, (String) creditType.getSelectedItem(), 0.0, amount));

            record.date = date;
            record.description = description;
            record.lines = updatedLines;

            journalRef.saveChangesFromColumnar();
            rebuildWorksheet();
            JOptionPane.showMessageDialog(this, "Adjusting Entry ID " + record.id + " has been updated successfully!", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static class PillLabel extends JLabel {
        public PillLabel(String text) {
            super(text);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(new EmptyBorder(6, 16, 6, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
            g2.dispose();
            super.paintComponent(g);
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
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
        }
    }

    public static class FrozenTablePane extends JPanel {
        private final JTable frozenTable;
        private final JTable mainTable;
        private final JScrollPane mainScrollPane;
        private final JScrollPane frozenScrollPane;
        private JComponent cornerComponent;

        public FrozenTablePane(JTable frozenTable, JTable mainTable) {
            this.frozenTable = frozenTable;
            this.mainTable = mainTable;

            setLayout(new BorderLayout());
            setBackground(Color.WHITE);

            mainScrollPane = new JScrollPane(mainTable,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
            mainScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            mainScrollPane.getViewport().setBackground(Color.WHITE);
            mainScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);

            frozenScrollPane = new JScrollPane(frozenTable,
                    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            frozenScrollPane.setBorder(BorderFactory.createEmptyBorder());
            frozenScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
            frozenScrollPane.getViewport().setBackground(Color.WHITE);
            frozenScrollPane.setPreferredSize(new Dimension(220, 0));

            // Reserve the same 44-pixel header area above the frozen table
            // as the custom two-row header above the financial table.
            if (cornerComponent != null) {
                frozenScrollPane.setColumnHeaderView(cornerComponent);
            }

            syncVerticalScrolling();
            syncRowHeights();

            FrozenViewportView frozenView = new FrozenViewportView(frozenTable);
            frozenScrollPane.setViewportView(frozenView);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setBackground(Color.WHITE);
            centerPanel.add(frozenScrollPane, BorderLayout.WEST);
            centerPanel.add(mainScrollPane, BorderLayout.CENTER);

            add(centerPanel, BorderLayout.CENTER);
        }

        private void syncVerticalScrolling() {
            JScrollBar mainVScroll = mainScrollPane.getVerticalScrollBar();
            JScrollBar frozenVScroll = frozenScrollPane.getVerticalScrollBar();
            frozenVScroll.setModel(mainVScroll.getModel());
        }

        private void syncRowHeights() {
            mainTable.addPropertyChangeListener("rowHeight", evt -> frozenTable.setRowHeight(mainTable.getRowHeight()));
            mainTable.getModel().addTableModelListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    for (int row = 0; row < mainTable.getRowCount(); row++) {
                        int h = mainTable.getRowHeight(row);
                        if (h != frozenTable.getRowHeight(row)) {
                            frozenTable.setRowHeight(row, h);
                        }
                    }
                });
            });
        }

        public void setCornerComponent(JComponent corner) {
            this.cornerComponent = corner;
            if (corner != null) {
                // The account-title header belongs above the frozen table,
                // not in the main scroll pane's corner.
                frozenScrollPane.setColumnHeaderView(corner);
            }
        }

        private static class FrozenViewportView extends JPanel {
            private final JTable table;

            public FrozenViewportView(JTable table) {
                this.table = table;
                setLayout(new BorderLayout());
                setBackground(Color.WHITE);
                add(table, BorderLayout.CENTER);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();

                g2.setColor(new Color(210, 210, 210));
                g2.drawLine(w - 1, 0, w - 1, h);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                Dimension d = table.getPreferredSize();
                return new Dimension(d.width, d.height);
            }
        }
    }
}