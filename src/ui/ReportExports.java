package ui;

import home.journal;

import javax.swing.table.TableModel;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ReportExports - NEW class. One method per exportable screen.
 *
 * Each screen has two kinds of methods here:
 *   exportXxx(...)  - used by the button: builds the rows, then opens the Save dialog.
 *   xxxHeaders() / xxxRows(...) - build the CSV content only (no windows), so they can be tested.
 *
 * The data is taken from the SAME objects the screens already use
 * (journal.allEntries and the worksheet's table models). No numbers are recalculated
 * here except the running balance in the ledger (see ledgerRows).
 */
public class ReportExports {

    // ============================================================ General Journal

    public static void exportJournal(Component parent, journal journalRef) {
        CsvExporter.exportRows(parent, journalHeaders(), journalRows(journalRef), "General_Journal.csv");
    }

    public static String[] journalHeaders() {
        return new String[]{"ID", "Date", "Description", "Category", "Account", "Type",
                "Debit", "Credit", "Status", "Void Reason"};
    }

    public static List<String[]> journalRows(journal journalRef) {
        List<String[]> rows = new ArrayList<>();
        for (journal.JournalRecord rec : journalRef.allEntries) {
            String status = rec.isVoided ? "VOIDED" : "ACTIVE";
            String category = rec.isAdjusting ? "Adjusting Entry" : "Regular Entry";
            String reason = rec.voidReason == null ? "" : rec.voidReason;
            for (journal.EntryLine line : rec.lines) {
                rows.add(new String[]{String.valueOf(rec.id), rec.date, rec.description, category,
                        line.account, line.type, money(line.debit), money(line.credit), status, reason});
            }
        }
        return rows;
    }

    // ============================================================ General Ledger

    public static void exportLedger(Component parent, journal journalRef) {
        CsvExporter.exportRows(parent, ledgerHeaders(), ledgerRows(journalRef), "General_Ledger.csv");
    }

    public static String[] ledgerHeaders() {
        return new String[]{"Account Title", "Date", "Explanation", "Debit", "Credit",
                "Running Balance (Debit - Credit)"};
    }

    /**
     * Uses the same entries as the Ledger screen (voided and adjusting entries are skipped).
     * The screen shows T-accounts; the CSV lists the same entries one per row, account by account.
     * Running balance = total debits so far minus total credits so far (a negative number
     * means the account has a credit balance).
     */
    public static List<String[]> ledgerRows(journal journalRef) {
        Map<String, List<double[]>> amounts = new TreeMap<>();      // account -> {debit, credit}
        Map<String, List<String[]>> texts = new TreeMap<>();        // account -> {date, explanation}
        for (journal.JournalRecord rec : journalRef.allEntries) {
            if (rec.isVoided || rec.isAdjusting) continue;
            for (journal.EntryLine line : rec.lines) {
                if (line.account == null || line.account.trim().isEmpty()) continue;
                String name = line.account.trim().toUpperCase();
                amounts.computeIfAbsent(name, k -> new ArrayList<>()).add(new double[]{line.debit, line.credit});
                texts.computeIfAbsent(name, k -> new ArrayList<>()).add(new String[]{rec.date, rec.description});
            }
        }

        List<String[]> rows = new ArrayList<>();
        for (String name : amounts.keySet()) {
            double running = 0, totalDr = 0, totalCr = 0;
            List<double[]> a = amounts.get(name);
            List<String[]> t = texts.get(name);
            for (int i = 0; i < a.size(); i++) {
                running += a.get(i)[0] - a.get(i)[1];
                totalDr += a.get(i)[0];
                totalCr += a.get(i)[1];
                rows.add(new String[]{name, t.get(i)[0], t.get(i)[1],
                        blankIfZero(a.get(i)[0]), blankIfZero(a.get(i)[1]), money(running)});
            }
            // Same TOTAL and BALANCE lines that the screen shows under each account
            rows.add(new String[]{name, "", "TOTAL", money(totalDr), money(totalCr), ""});
            double balance = Math.abs(totalDr - totalCr);
            if (totalDr >= totalCr) {
                rows.add(new String[]{name, "", "BALANCE", money(balance), "", ""});
            } else {
                rows.add(new String[]{name, "", "BALANCE", "", money(balance), ""});
            }
        }
        return rows;
    }

    // ============================================================ 12-column sheet (Columnar Worksheet)

    public static void exportWorksheet(Component parent, TableModel accountNames, TableModel numbers) {
        CsvExporter.exportRows(parent, worksheetHeaders(numbers), worksheetRows(accountNames, numbers),
                "Columnar_Worksheet.csv");
    }

    public static String[] worksheetHeaders(TableModel numbers) {
        String[] headers = new String[numbers.getColumnCount() + 1];
        headers[0] = "Account Title";
        for (int c = 0; c < numbers.getColumnCount(); c++) {
            headers[c + 1] = numbers.getColumnName(c);
        }
        return headers;
    }

    /** Every row currently shown on the sheet, including TOTAL, NET INCOME and GRAND TOTAL. */
    public static List<String[]> worksheetRows(TableModel accountNames, TableModel numbers) {
        List<String[]> rows = new ArrayList<>();
        for (int r = 0; r < numbers.getRowCount(); r++) {
            String[] row = new String[numbers.getColumnCount() + 1];
            row[0] = text(accountNames.getValueAt(r, 0));
            for (int c = 0; c < numbers.getColumnCount(); c++) {
                row[c + 1] = text(numbers.getValueAt(r, c));
            }
            rows.add(row);
        }
        return rows;
    }

    // ============================================================ Trial Balance (TB columns of the sheet)

    public static void exportTrialBalance(Component parent, TableModel accountNames, TableModel numbers) {
        CsvExporter.exportRows(parent, trialBalanceHeaders(), trialBalanceRows(accountNames, numbers),
                "Trial_Balance.csv");
    }

    public static String[] trialBalanceHeaders() {
        return new String[]{"Account Title", "Debit", "Credit"};
    }

    /**
     * Takes the "TB Debit" and "TB Credit" columns (columns 0 and 1) of the 12-column sheet,
     * for the accounts that have a trial balance amount, and ends with the TOTAL row.
     */
    public static List<String[]> trialBalanceRows(TableModel accountNames, TableModel numbers) {
        List<String[]> rows = new ArrayList<>();
        for (int r = 0; r < numbers.getRowCount(); r++) {
            String title = text(accountNames.getValueAt(r, 0));
            String dr = text(numbers.getValueAt(r, 0));
            String cr = text(numbers.getValueAt(r, 1));
            if (title.equals("TOTAL")) {
                rows.add(new String[]{"TOTAL", dr, cr});
                break; // the rows after TOTAL (net income, grand total...) are not part of the trial balance
            }
            if (!dr.isEmpty() || !cr.isEmpty()) {
                rows.add(new String[]{title, dr, cr});
            }
        }
        return rows;
    }

    // ============================================================ small helpers

    private static String money(double amount) { return String.format("%.2f", amount); }

    private static String blankIfZero(double amount) { return amount == 0 ? "" : money(amount); }

    private static String text(Object value) { return value == null ? "" : value.toString(); }
}
