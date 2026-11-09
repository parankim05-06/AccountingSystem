import database.Database;
import model.Account;
import model.Formats;
import model.JournalEntry;
import model.JournalLine;
import model.User;
import model.WorksheetRow;
import dao.JournalDAO;
import service.AppException;
import service.AuthService;
import service.JournalService;
import service.LegacyImporter;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Simple tests with no extra libraries.
 *
 *   java -cp "bin;lib\*" TestRunner          runs every test
 *   java -cp "bin;lib\*" TestRunner write    restart test, step 1 (saves data)
 *   java -cp "bin;lib\*" TestRunner read     restart test, step 2 (a NEW program run reads it back)
 *
 * Tests use their own temporary database files. Your real DAPAS database is never touched.
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    interface Action { void run() throws Exception; }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("write")) { restartStepWrite(); }
        else if (args.length > 0 && args[0].equals("read")) { restartStepRead(); }
        else { runAllTests(); }

        System.out.println();
        System.out.println("RESULT: " + passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void runAllTests() throws Exception {
        testFormats();
        testDatabaseSetup();
        testRegistrationAndLogin();
        testJournalWorkflow();
        testRollback();
        testLedgerAndWorksheet();
        testLegacyImport();
    }

    // ------------------------------------------------------------------ helpers

    private static void freshDatabase() throws Exception {
        File file = File.createTempFile("dapas-test-", ".db");
        file.delete();
        file.deleteOnExit();
        Database.setDatabasePath(file.getAbsolutePath());
        Database.initialize();
    }

    private static void check(String name, boolean condition) {
        if (condition) { passed++; System.out.println("  PASS  " + name); }
        else           { failed++; System.out.println("  FAIL  " + name); }
    }

    private static void checkEquals(String name, Object expected, Object actual) {
        boolean same = expected == null ? actual == null : expected.equals(actual);
        if (same) { passed++; System.out.println("  PASS  " + name); }
        else {
            failed++;
            System.out.println("  FAIL  " + name + "   (expected: " + expected + ", got: " + actual + ")");
        }
    }

    /** The action must throw an AppException whose message starts with expectedStart. */
    private static void expectAppError(String name, String expectedStart, Action action) {
        try {
            action.run();
            failed++;
            System.out.println("  FAIL  " + name + "   (no error was thrown)");
        } catch (AppException e) {
            checkEquals(name, true, e.getMessage().startsWith(expectedStart));
            if (!e.getMessage().startsWith(expectedStart)) System.out.println("        got: " + e.getMessage());
        } catch (Exception e) {
            failed++;
            System.out.println("  FAIL  " + name + "   (wrong error type: " + e + ")");
        }
    }

    private static long count(String sql) throws Exception {
        try (Connection c = Database.getConnection(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) { r.next(); return r.getLong(1); }
    }

    private static Account account(JournalService service, String name) throws Exception {
        for (Account a : service.getActiveAccounts()) if (a.getName().equals(name)) return a;
        throw new IllegalStateException("account not found: " + name);
    }

    private static JournalEntry simpleEntry(String date, String description,
                                            Account debit, Account credit, long centavos) {
        JournalEntry entry = new JournalEntry();
        entry.setEntryDate(date);
        entry.setDescription(description);
        entry.addLine(new JournalLine(debit.getId(), centavos, 0));
        entry.addLine(new JournalLine(credit.getId(), 0, centavos));
        return entry;
    }

    // ------------------------------------------------------------------ tests

    private static void testFormats() {
        System.out.println("Money and date formatting");
        checkEquals("parse 1,500.50", 150050L, Formats.parseMoney("1,500.50"));
        checkEquals("parse 1500", 150000L, Formats.parseMoney("1500"));
        checkEquals("parse 0.10 + 0.20 is exactly 0.30", 30L, Formats.parseMoney("0.10") + Formats.parseMoney("0.20"));
        checkEquals("format 150050", "\u20B11,500.50", Formats.money(150050));
        checkEquals("format 5", "\u20B10.05", Formats.money(5));
        checkEquals("format big", "\u20B112,345,678.90", Formats.money(1234567890));
        checkEquals("date to ISO", "2026-10-27", Formats.displayToIsoDate("27-10-2026"));
        checkEquals("date to display", "27-10-2026", Formats.isoToDisplayDate("2026-10-27"));
        for (String bad : new String[]{"abc", "", "-5", "0", "1.234", "12 34"}) {
            try { Formats.parseMoney(bad); check("reject amount '" + bad + "'", false); }
            catch (IllegalArgumentException e) { check("reject amount '" + bad + "'", true); }
        }
        for (String bad : new String[]{"31-02-2026", "2026-10-27", "", "99-99-9999"}) {
            try { Formats.displayToIsoDate(bad); check("reject date '" + bad + "'", false); }
            catch (IllegalArgumentException e) { check("reject date '" + bad + "'", true); }
        }
    }

    private static void testDatabaseSetup() throws Exception {
        System.out.println("Database setup (SQLite initializes)");
        freshDatabase();
        checkEquals("4 tables created", 4L, count(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name IN "
          + "('users','accounts','journal_entries','journal_lines')"));
        checkEquals("starter chart has 13 accounts", 13L, count("SELECT COUNT(*) FROM accounts"));

        Database.initialize(); // running it again must change nothing
        checkEquals("second initialize does not duplicate accounts", 13L, count("SELECT COUNT(*) FROM accounts"));

        try (Connection c = Database.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("INSERT INTO journal_entries (entry_date, description, status, created_at) "
                          + "VALUES ('2026-01-01','x','DRAFT','now')");
            try {
                s.executeUpdate("INSERT INTO journal_lines (journal_entry_id, account_id, debit, credit) "
                              + "VALUES (1, 99999, 100, 0)");
                check("foreign key rejects a line with a missing account", false);
            } catch (Exception e) {
                check("foreign key rejects a line with a missing account", true);
            }
            try {
                s.executeUpdate("INSERT INTO accounts (account_code, account_name, account_type) "
                              + "VALUES ('9999','Bad','Nonsense')");
                check("account type CHECK rejects an invalid type", false);
            } catch (Exception e) {
                check("account type CHECK rejects an invalid type", true);
            }
        }
    }

    private static void testRegistrationAndLogin() throws Exception {
        System.out.println("Registration and login");
        freshDatabase();
        AuthService auth = new AuthService();

        auth.register("maria", "secret123");
        User user = auth.login("maria", "secret123");
        checkEquals("login with the right password works", "maria", user.getUsername());
        check("password is not stored in plain text", !user.getPasswordHash().contains("secret123"));
        check("hash format is iterations:salt:hash", user.getPasswordHash().split(":").length == 3);

        auth.register("juan", "secret123");
        check("same password gives a different hash (salt)",
              !auth.login("juan", "secret123").getPasswordHash().equals(user.getPasswordHash()));

        expectAppError("wrong password is rejected", "Invalid username or password.", () -> auth.login("maria", "wrong"));
        expectAppError("unknown user is rejected", "Invalid username or password.", () -> auth.login("nobody", "secret123"));
        expectAppError("duplicate username is rejected", "Username already exists!", () -> auth.register("maria", "another123"));
        expectAppError("empty fields are rejected", "Fields cannot be empty.", () -> auth.register("", ""));
        expectAppError("short password is rejected", "Password must be at least", () -> auth.register("pedro", "abc"));
    }

    private static void testJournalWorkflow() throws Exception {
        System.out.println("Journal: balanced, unbalanced, posting");
        freshDatabase();
        JournalService journal = new JournalService();
        Account cash = account(journal, "Cash");
        Account capital = account(journal, "Owner's Capital");

        // The example from the guide: debit Cash 5,000.00 and credit Owner's Capital 5,000.00
        int id = journal.saveDraft(simpleEntry("2026-10-01", "Owner invested cash", cash, capital, 500000));
        check("balanced entry is saved", id > 0);
        checkEquals("saved entry starts as DRAFT", "DRAFT", journal.getAllEntries().get(0).getStatus());
        checkEquals("entry saved with 2 lines", 2, journal.getAllEntries().get(0).getLines().size());
        checkEquals("draft does NOT appear in the ledger", 0, journal.getLedger().size());

        long entriesBefore = count("SELECT COUNT(*) FROM journal_entries");
        long linesBefore = count("SELECT COUNT(*) FROM journal_lines");

        JournalEntry unbalanced = new JournalEntry();
        unbalanced.setEntryDate("2026-10-02");
        unbalanced.setDescription("Does not balance");
        unbalanced.addLine(new JournalLine(cash.getId(), 100000, 0));
        unbalanced.addLine(new JournalLine(capital.getId(), 0, 90000));
        expectAppError("unbalanced entry is rejected", "Debit and credit totals must be equal.",
                       () -> journal.saveDraft(unbalanced));
        checkEquals("rejected entry saved nothing (entries)", entriesBefore, count("SELECT COUNT(*) FROM journal_entries"));
        checkEquals("rejected entry saved nothing (lines)", linesBefore, count("SELECT COUNT(*) FROM journal_lines"));

        expectAppError("missing account is rejected", "Please select an account.",
                       () -> journal.saveDraft(simpleEntry("2026-10-02", "x", Account.placeholder(), capital, 100)));
        expectAppError("missing description is rejected", "Please enter a description.",
                       () -> journal.saveDraft(simpleEntry("2026-10-02", "  ", cash, capital, 100)));

        JournalEntry oneLine = new JournalEntry();
        oneLine.setEntryDate("2026-10-02");
        oneLine.setDescription("Only one line");
        oneLine.addLine(new JournalLine(cash.getId(), 100, 0));
        expectAppError("single-line entry is rejected", "A journal entry needs at least two lines",
                       () -> journal.saveDraft(oneLine));

        JournalEntry bothSides = new JournalEntry();
        bothSides.setEntryDate("2026-10-02");
        bothSides.setDescription("Both sides on one line");
        bothSides.addLine(new JournalLine(cash.getId(), 100, 100));
        bothSides.addLine(new JournalLine(capital.getId(), 0, 0));
        expectAppError("line with debit AND credit is rejected", "Each line needs either a debit or a credit",
                       () -> journal.saveDraft(bothSides));

        journal.postEntry(id);
        checkEquals("posted entry has status POSTED", "POSTED", journal.getAllEntries().get(0).getStatus());
        expectAppError("posting twice is rejected", "This journal entry has already been posted.",
                       () -> journal.postEntry(id));
        expectAppError("posting a missing entry is rejected", "That journal entry could not be found.",
                       () -> journal.postEntry(424242));

        Map<String, List<JournalLine>> ledger = journal.getLedger();
        checkEquals("ledger now has 2 accounts", 2, ledger.size());
        checkEquals("ledger: Cash debit", 500000L, ledger.get("1000 - Cash").get(0).getDebit());
        checkEquals("ledger: Capital credit", 500000L, ledger.get("3000 - Owner's Capital").get(0).getCredit());
    }

    private static void testRollback() throws Exception {
        System.out.println("Transactions: failure rolls everything back");
        freshDatabase();
        JournalService journal = new JournalService();
        Account cash = account(journal, "Cash");

        // Line 1 is fine, line 2 points at an account that does not exist, so the database refuses it
        // AFTER the entry row and line 1 were already written. Rollback must remove them.
        JournalEntry broken = new JournalEntry();
        broken.setEntryDate("2026-10-03");
        broken.setDescription("Will fail halfway");
        broken.setCreatedAt("2026-10-03T10:00:00");
        broken.addLine(new JournalLine(cash.getId(), 1000, 0));
        broken.addLine(new JournalLine(99999, 0, 1000));
        try {
            new JournalDAO().insert(broken);
            check("insert with a bad account fails", false);
        } catch (Exception e) {
            check("insert with a bad account fails", true);
        }
        checkEquals("no half-saved entry remains", 0L, count("SELECT COUNT(*) FROM journal_entries"));
        checkEquals("no half-saved line remains", 0L, count("SELECT COUNT(*) FROM journal_lines"));
    }

    private static void testLedgerAndWorksheet() throws Exception {
        System.out.println("Ledger and worksheet come from the same saved data");
        freshDatabase();
        JournalService journal = new JournalService();
        Account cash = account(journal, "Cash");
        Account capital = account(journal, "Owner's Capital");
        Account rent = account(journal, "Rent Expense");
        Account revenue = account(journal, "Service Revenue");

        journal.postEntry(journal.saveDraft(simpleEntry("2026-10-01", "Owner invested cash", cash, capital, 500000)));
        journal.postEntry(journal.saveDraft(simpleEntry("2026-10-05", "Paid rent", rent, cash, 120000)));
        journal.postEntry(journal.saveDraft(simpleEntry("2026-10-09", "Earned fees", cash, revenue, 250050)));
        journal.saveDraft(simpleEntry("2026-10-10", "Still a draft", cash, revenue, 99999999)); // must be ignored

        List<JournalLine> cashLines = journal.getLedger().get("1000 - Cash");
        checkEquals("Cash has 3 posted lines (draft ignored)", 3, cashLines.size());
        long cashDebits = 0, cashCredits = 0;
        for (JournalLine line : cashLines) { cashDebits += line.getDebit(); cashCredits += line.getCredit(); }
        checkEquals("Cash total debits", 750050L, cashDebits);
        checkEquals("Cash total credits", 120000L, cashCredits);

        List<WorksheetRow> rows = journal.getWorksheet();
        WorksheetRow total = rows.get(rows.size() - 1);
        checkEquals("last worksheet row is TOTAL", "TOTAL", total.accountName);
        checkEquals("trial balance debits = credits", total.trialDebit, total.trialCredit);
        checkEquals("trial balance total (630,050 Cash + 120,000 Rent)", 750050L, total.trialDebit);
        for (WorksheetRow row : rows) {
            if (row.accountName.equals("Cash"))            checkEquals("Cash balance sheet debit", 630050L, row.balanceDebit);
            if (row.accountName.equals("Rent Expense"))    checkEquals("Rent in income statement debit", 120000L, row.incomeDebit);
            if (row.accountName.equals("Service Revenue")) checkEquals("Revenue in income statement credit", 250050L, row.incomeCredit);
            if (row.accountName.equals("Owner's Capital")) checkEquals("Capital in owner's equity credit", 500000L, row.equityCredit);
        }
    }

    private static void testLegacyImport() throws Exception {
        System.out.println("Importing the old text-file data");
        freshDatabase();

        File folder = Files.createTempDirectory("dapas-legacy-").toFile();
        Files.write(new File(folder, "users.txt").toPath(), "admin,admin123\nmaria,pass1234\n".getBytes("UTF-8"));
        String journalText =
            "1|05-10-2026|Owner invested|Cash,ASSET,50000.0,0.0;Owner's Capital,EQUITY,0.0,50000.0\n"
          + "2|06-10-2026|Sold services|Cash,ASSET,1500.5,0.0;Fees Income,INCOME,0.0,1500.5\n"
          + "this line is damaged\n";
        Files.write(new File(folder, "journal_admin_default.txt").toPath(), journalText.getBytes("UTF-8"));

        String report = new LegacyImporter().importFrom(folder);
        System.out.println("        report: " + report);
        check("importer reports what it did", report.startsWith("Imported 2 user(s) and 2 journal"));
        check("old password was hashed during import", new AuthService().login("admin", "admin123") != null);
        checkEquals("2 users imported", 2L, count("SELECT COUNT(*) FROM users"));
        checkEquals("2 entries imported as POSTED", 2L, count("SELECT COUNT(*) FROM journal_entries WHERE status='POSTED'"));
        checkEquals("Cash was matched, not duplicated", 1L, count("SELECT COUNT(*) FROM accounts WHERE account_name='Cash'"));
        checkEquals("unknown account 'Fees Income' was created as Revenue", 1L,
                    count("SELECT COUNT(*) FROM accounts WHERE account_name='Fees Income' AND account_type='Revenue' AND account_code='4010'"));
        checkEquals("1500.5 became exactly 150050 centavos", 150050L,
                    count("SELECT debit FROM journal_lines WHERE debit = 150050"));
        checkEquals("date converted to ISO", 1L, count("SELECT COUNT(*) FROM journal_entries WHERE entry_date='2026-10-05'"));
        check("old users file renamed, not deleted", new File(folder, "users.txt.imported").exists() && !new File(folder, "users.txt").exists());
        check("old journal file renamed, not deleted", new File(folder, "journal_admin_default.txt.imported").exists());
        checkEquals("running the importer again does nothing", "", new LegacyImporter().importFrom(folder));
        checkEquals("still 2 entries after the second run", 2L, count("SELECT COUNT(*) FROM journal_entries"));
    }

    // ------------------------------------------------------------------ restart test (two separate program runs)

    private static File restartDatabaseFile() {
        return new File(System.getProperty("java.io.tmpdir"), "dapas-restart-test.db");
    }

    private static void restartStepWrite() throws Exception {
        System.out.println("Restart test, step 1: save data");
        restartDatabaseFile().delete();
        Database.setDatabasePath(restartDatabaseFile().getAbsolutePath());
        Database.initialize();

        new AuthService().register("persist", "secret123");
        JournalService journal = new JournalService();
        int id = journal.saveDraft(simpleEntry("2026-10-01", "Survives restart",
                account(journal, "Cash"), account(journal, "Owner's Capital"), 123456));
        journal.postEntry(id);
        check("data written", true);
    }

    private static void restartStepRead() throws Exception {
        System.out.println("Restart test, step 2: a NEW program run reads the data back");
        Database.setDatabasePath(restartDatabaseFile().getAbsolutePath());
        Database.initialize(); // normal startup again

        checkEquals("user is still there and can log in", "persist", new AuthService().login("persist", "secret123").getUsername());
        JournalService journal = new JournalService();
        checkEquals("entry is still there", 1, journal.getAllEntries().size());
        checkEquals("entry is still POSTED", "POSTED", journal.getAllEntries().get(0).getStatus());
        checkEquals("ledger still shows the amount", 123456L, journal.getLedger().get("1000 - Cash").get(0).getDebit());
        checkEquals("starter accounts were not added twice", 13L, count("SELECT COUNT(*) FROM accounts"));
    }
}
