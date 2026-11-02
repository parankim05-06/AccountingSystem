package ui;

import model.Formats;
import model.User;
import model.WorksheetRow;
import service.AppException;
import service.JournalService;

import javax.swing.BorderFactory;
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
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * The 12 COLUMNAR SHEET (worksheet). It only DISPLAYS the rows that
 * JournalService.getWorksheet() calculates from the posted journal lines.
 */
public class ColumnarFrame extends JFrame {

    private final User user;
    private final JournalService journalService = new JournalService();
    private DefaultTableModel tableModel;

    public ColumnarFrame(User user) {
        this.user = user;
        setTitle("Accounting System Workspace");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(Theme.WARM_CREAM);
        root.add(buildTopArea(), BorderLayout.NORTH);
        root.add(buildTableCard(), BorderLayout.CENTER);
        add(root);

        loadWorksheet();
    }

    private JPanel buildTopArea() {
        JLabel moduleTitle = new JLabel("12 COLUMNAR SHEET");
        moduleTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        moduleTitle.setForeground(Theme.DEEP_MOCHA);

        JButton refreshButton = Theme.primaryButton("Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        refreshButton.addActionListener(e -> {
            loadWorksheet();
            JOptionPane.showMessageDialog(this, "12 Columnar Sheet refreshed!");
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
        topArea.add(new NavBar(this, user, NavBar.COLUMNAR), BorderLayout.NORTH);
        topArea.add(topHeader, BorderLayout.SOUTH);
        return topArea;
    }

    private JPanel buildTableCard() {
        String[] columns = {
            "Account Title",
            "Trial Balance Dr", "Trial Balance Cr",
            "Adjustments Dr", "Adjustments Cr",
            "Adjusted Trial Balance Dr", "Adjusted Trial Balance Cr",
            "Income Statement Dr", "Income Statement Cr",
            "Owner's Equity Dr", "Owner's Equity Cr",
            "Balance Sheet Dr", "Balance Sheet Cr"
        };
        tableModel = Theme.readOnlyTableModel(columns);

        JTable table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));

        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(JLabel.RIGHT);
        for (int i = 1; i < columns.length; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightAlign);
            table.getColumnModel().getColumn(i).setPreferredWidth(130);
        }
        table.getColumnModel().getColumn(0).setPreferredWidth(160);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.SOFT_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.WARM_BEIGE, 1),
                new EmptyBorder(15, 15, 15, 15)));
        card.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        return card;
    }

    private void loadWorksheet() {
        tableModel.setRowCount(0);
        try {
            for (WorksheetRow row : journalService.getWorksheet()) {
                tableModel.addRow(new Object[]{
                    row.accountName,
                    Formats.moneyOrBlank(row.trialDebit),    Formats.moneyOrBlank(row.trialCredit),
                    Formats.moneyOrBlank(row.adjustDebit),   Formats.moneyOrBlank(row.adjustCredit),
                    Formats.moneyOrBlank(row.adjustedDebit), Formats.moneyOrBlank(row.adjustedCredit),
                    Formats.moneyOrBlank(row.incomeDebit),   Formats.moneyOrBlank(row.incomeCredit),
                    Formats.moneyOrBlank(row.equityDebit),   Formats.moneyOrBlank(row.equityCredit),
                    Formats.moneyOrBlank(row.balanceDebit),  Formats.moneyOrBlank(row.balanceCredit)
                });
            }
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
