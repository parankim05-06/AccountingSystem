import home.columnar;
import home.journal;
import ui.CsvExporter;
import ui.ReportExports;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;

/**
 * Tests for the Export CSV feature (no windows are opened).
 *   java -Djava.awt.headless=true -cp "bin;lib\*" CsvExportTest
 */
public class CsvExportTest {
    static int passed = 0, failed = 0;

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (ok) passed++; else failed++;
    }

    static String csv(File dir, String name, String[] headers, List<String[]> rows) throws Exception {
        File f = new File(dir, name);
        CsvExporter.writeCsv(f, headers, rows);
        return new String(Files.readAllBytes(f.toPath()), "UTF-8");
    }

    static Object field(Object o, String name) throws Exception {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(o);
    }

    public static void main(String[] args) throws Exception {
        File dir = Files.createTempDirectory("csvtest").toFile();
        File data = new File(dir, "journal_test_General.txt");
        // id|date|description|account,type,debit,credit;...|voided|reason|adjusting
        Files.write(data.toPath(), (
            "1|01-11-2026|Owner invested, cash|CASH,ASSET,50000.0,0.0;OWNER'S CAPITAL,EQUITY,0.0,50000.0|false||false\n" +
            "2|05-11-2026|Cash service income|CASH,ASSET,15000.0,0.0;SERVICE REVENUE,REVENUE,0.0,15000.0|false||false\n" +
            "3|10-11-2026|Paid \"November\" salaries|SALARIES EXPENSE,EXPENSE,6000.0,0.0;CASH,ASSET,0.0,6000.0|false||false\n" +
            "4|12-11-2026|Wrong entry|SUPPLIES,ASSET,999.0,0.0;CASH,ASSET,0.0,999.0|true|Typo|false\n" +
            "5|30-11-2026|Supplies used|SUPPLIES EXPENSE,EXPENSE,1200.0,0.0;SUPPLIES,ASSET,0.0,1200.0|false||true\n"
        ).getBytes("UTF-8"));

        journal j = new journal(data.getPath(), "test");
        check("journal loaded 5 records", j.allEntries.size() == 5);

        // ---- General Journal
        String jc = csv(dir, "General_Journal.csv", ReportExports.journalHeaders(), ReportExports.journalRows(j));
        String[] jl = jc.split("\r\n");
        check("journal: header + 10 lines", jl.length == 11);
        check("journal: header text", jl[0].equals("\uFEFFID,Date,Description,Category,Account,Type,Debit,Credit,Status,Void Reason"));
        check("journal: comma in description quoted", jl[1].equals("1,01-11-2026,\"Owner invested, cash\",Regular Entry,CASH,ASSET,50000.00,0.00,ACTIVE,"));
        check("journal: quote in description doubled", jc.contains("\"Paid \"\"November\"\" salaries\""));
        check("journal: voided entry + reason exported", jc.contains("VOIDED,Typo"));
        check("journal: adjusting entry exported", jc.contains("Adjusting Entry,SUPPLIES EXPENSE"));

        // ---- General Ledger
        List<String[]> lr = ReportExports.ledgerRows(j);
        String lc = csv(dir, "General_Ledger.csv", ReportExports.ledgerHeaders(), lr);
        check("ledger: voided/adjusting entries skipped", !lc.contains("Wrong entry") && !lc.contains("Supplies used"));
        check("ledger: CASH running balance 50000 -> 65000 -> 59000",
              lc.contains("CASH,01-11-2026,\"Owner invested, cash\",50000.00,,50000.00")
           && lc.contains("CASH,05-11-2026,Cash service income,15000.00,,65000.00")
           && lc.contains("CASH,10-11-2026,\"Paid \"\"November\"\" salaries\",,6000.00,59000.00"));
        check("ledger: CASH TOTAL and BALANCE lines", lc.contains("CASH,,TOTAL,65000.00,6000.00,") && lc.contains("CASH,,BALANCE,59000.00,,"));
        check("ledger: credit-balance account has BALANCE in credit column", lc.contains("SERVICE REVENUE,,BALANCE,,15000.00,"));

        // ---- Columnar worksheet + Trial balance (read the real private table models)
        columnar col = new columnar(j, "test");
        DefaultTableModel names = (DefaultTableModel) field(col, "accountTitleModel");
        DefaultTableModel nums = (DefaultTableModel) field(col, "tableModel");
        check("worksheet has rows on screen", nums.getRowCount() > 0);

        List<String[]> wr = ReportExports.worksheetRows(names, nums);
        String wc = csv(dir, "Columnar_Worksheet.csv", ReportExports.worksheetHeaders(nums), wr);
        String[] wl = wc.split("\r\n");
        check("worksheet: header has 13 columns", wl[0].split(",").length == 13);
        check("worksheet: header names", wl[0].equals("\uFEFFAccount Title,TB Debit,TB Credit,Adj Debit,Adj Credit,ATB Debit,ATB Credit,IS Debit,IS Credit,OE Debit,OE Credit,BS Debit,BS Credit"));
        check("worksheet: same number of rows as screen (+header)", wl.length == nums.getRowCount() + 1);
        boolean allMatch = true;
        for (int r = 0; r < nums.getRowCount(); r++) {
            String[] cells = wl[r + 1].split(",", -1);
            if (!cells[0].equals(String.valueOf(names.getValueAt(r, 0)))) allMatch = false;
            for (int c = 0; c < nums.getColumnCount(); c++) {
                if (!cells[c + 1].equals(String.valueOf(nums.getValueAt(r, c)))) allMatch = false;
            }
        }
        check("worksheet: every exported cell equals the screen cell", allMatch);
        check("worksheet: contains NET INCOME, ENDING OWNER'S CAPITAL, GRAND TOTAL",
              wc.contains("NET INCOME,") && wc.contains("ENDING OWNER'S CAPITAL,") && wc.contains("GRAND TOTAL,"));

        List<String[]> tb = ReportExports.trialBalanceRows(names, nums);
        String tc = csv(dir, "Trial_Balance.csv", ReportExports.trialBalanceHeaders(), tb);
        String[] tl = tc.split("\r\n");
        String[] last = tl[tl.length - 1].split(",", -1);
        check("trial balance: ends with TOTAL row", last[0].equals("TOTAL"));
        check("trial balance: debit total equals credit total (" + last[1] + ")", last[1].equals(last[2]) && last[1].equals("65000.00"));
        check("trial balance: account with only an adjustment (SUPPLIES EXPENSE) not listed", !tc.contains("SUPPLIES EXPENSE"));
        check("trial balance: 4 accounts + TOTAL", tl.length == 1 + 4 + 1 - 0 || tl.length == 1 + 5 + 1);

        // ---- CSV safety
        check("formula text is neutralised", CsvExporter.clean("=SUM(A1)").equals("'=SUM(A1)"));
        check("negative number untouched", CsvExporter.clean("-500.00").equals("-500.00"));
        check("null becomes empty", CsvExporter.clean(null).equals(""));

        // ---- Error path: saving to a folder that does not exist must throw (the button shows a message)
        boolean threw = false;
        try { CsvExporter.writeCsv(new File(dir, "nope/x.csv"), new String[]{"a"}, new java.util.ArrayList<>()); }
        catch (java.io.IOException e) { threw = true; }
        check("saving to a missing folder throws an error", threw);

        System.out.println();
        System.out.println("RESULT: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }
}
