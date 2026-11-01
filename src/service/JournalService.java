package service;

import dao.AccountDAO;
import dao.JournalDAO;
import model.Account;
import model.Formats;
import model.JournalEntry;
import model.JournalLine;
import model.WorksheetRow;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The accounting rules. Screens call this class; this class calls the DAOs.
 *
 * Rules enforced here:
 *   - an entry needs a date, a description and at least two lines
 *   - every line needs an account and EITHER a debit OR a credit
 *   - debit and credit accounts must be different
 *   - total debits must equal total credits
 *   - a DRAFT can be posted once; a POSTED entry cannot be posted again
 * The ledger and the worksheet are built ONLY from POSTED entries.
 */
public class JournalService {

    private final JournalDAO journalDAO = new JournalDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public List<Account> getActiveAccounts() throws AppException {
        try {
            return accountDAO.findActive();
        } catch (SQLException e) {
            throw new AppException("Unable to load the accounts. Please try again.", e);
        }
    }

    /** Checks the entry, then saves it as a DRAFT. Returns the new entry id. */
    public int saveDraft(JournalEntry entry) throws AppException {
        validate(entry);
        entry.setStatus(JournalEntry.DRAFT);
        entry.setCreatedAt(LocalDateTime.now().withNano(0).toString());

        try {
            return journalDAO.insert(entry);
        } catch (SQLException e) {
            throw new AppException("Unable to save the transaction. Please try again.", e);
        }
    }

    /** Changes a DRAFT to POSTED so it shows up in the ledger and worksheet. */
    public void postEntry(int entryId) throws AppException {
        try {
            JournalEntry entry = journalDAO.findById(entryId);

            if (entry == null) {
                throw new AppException("That journal entry could not be found.");
            }

            if (JournalEntry.POSTED.equals(entry.getStatus())) {
                throw new AppException("This journal entry has already been posted.");
            }

            validate(entry);

            if (journalDAO.markPosted(entryId) == 0) {
                throw new AppException("This journal entry has already been posted.");
            }
        } catch (SQLException e) {
            throw new AppException("Unable to post the entry. Please try again.", e);
        }
    }

    public List<JournalEntry> getAllEntries() throws AppException {
        try {
            return journalDAO.findAll();
        } catch (SQLException e) {
            throw new AppException("Unable to load the journal entries. Please try again.", e);
        }
    }

    /** The newest entries first (used by the dashboard). */
    public List<JournalEntry> getRecentEntries(int count) throws AppException {
        List<JournalEntry> all = getAllEntries();
        List<JournalEntry> newestFirst = new ArrayList<>();

        for (int i = all.size() - 1; i >= 0 && newestFirst.size() < count; i--) {
            newestFirst.add(all.get(i));
        }

        return newestFirst;
    }

    /**
     * The ledger: posted lines grouped by account.
     */
    public Map<String, List<JournalLine>> getLedger() throws AppException {
        Map<String, List<JournalLine>> ledger = new LinkedHashMap<>();

        try {
            for (JournalLine line : journalDAO.findPostedLines()) {
                String accountLabel =
                        line.getAccountCode() + " - " + line.getAccountName();

                ledger.computeIfAbsent(accountLabel, key -> new ArrayList<>()).add(line);
            }
        } catch (SQLException e) {
            throw new AppException("Unable to load the ledger. Please try again.", e);
        }

        return ledger;
    }

    /**
     * The 12-column worksheet, calculated from posted lines.
     */
    public List<WorksheetRow> getWorksheet() throws AppException {
        List<WorksheetRow> rows = new ArrayList<>();
        WorksheetRow total = new WorksheetRow();
        total.accountName = "TOTAL";

        try {
            for (JournalLine account : journalDAO.findPostedTotalsByAccount()) {
                long balance = account.getDebit() - account.getCredit();

                long debitBalance = balance > 0 ? balance : 0;
                long creditBalance = balance < 0 ? -balance : 0;

                WorksheetRow row = new WorksheetRow();
                row.accountName = account.getAccountName();

                row.trialDebit = debitBalance;
                row.trialCredit = creditBalance;

                row.adjustedDebit = debitBalance;
                row.adjustedCredit = creditBalance;

                String type = account.getAccountType();

                if (type.equals("Revenue") || type.equals("Expense")) {
                    row.incomeDebit = debitBalance;
                    row.incomeCredit = creditBalance;
                } else if (type.equals("Equity")) {
                    row.equityDebit = debitBalance;
                    row.equityCredit = creditBalance;
                } else {
                    row.balanceDebit = debitBalance;
                    row.balanceCredit = creditBalance;
                }

                addToTotal(total, row);
                rows.add(row);
            }
        } catch (SQLException e) {
            throw new AppException("Unable to load the worksheet. Please try again.", e);
        }

        rows.add(total);
        return rows;
    }

    private void addToTotal(WorksheetRow total, WorksheetRow row) {
        total.trialDebit += row.trialDebit;
        total.trialCredit += row.trialCredit;

        total.adjustDebit += row.adjustDebit;
        total.adjustCredit += row.adjustCredit;

        total.adjustedDebit += row.adjustedDebit;
        total.adjustedCredit += row.adjustedCredit;

        total.incomeDebit += row.incomeDebit;
        total.incomeCredit += row.incomeCredit;

        total.equityDebit += row.equityDebit;
        total.equityCredit += row.equityCredit;

        total.balanceDebit += row.balanceDebit;
        total.balanceCredit += row.balanceCredit;
    }

    /** Validates a journal entry and reports the first problem found. */
    private void validate(JournalEntry entry) throws AppException {
        if (entry.getEntryDate() == null
                || entry.getEntryDate().trim().isEmpty()) {
            throw new AppException("Please enter a date.");
        }

        if (entry.getDescription() == null
                || entry.getDescription().trim().isEmpty()) {
            throw new AppException("Please enter a description.");
        }

        if (entry.getLines() == null || entry.getLines().size() < 2) {
            throw new AppException(
                    "A journal entry needs at least two lines (one debit and one credit)."
            );
        }

        Integer debitAccountId = null;
        Integer creditAccountId = null;

        for (JournalLine line : entry.getLines()) {
            if (line.getAccountId() <= 0) {
                throw new AppException("Please select an account.");
            }

            boolean hasDebit = line.getDebit() > 0;
            boolean hasCredit = line.getCredit() > 0;

            if (hasDebit == hasCredit) {
                throw new AppException(
                        "Each line needs either a debit or a credit amount."
                );
            }

            if (hasDebit) {
                debitAccountId = line.getAccountId();
            }

            if (hasCredit) {
                creditAccountId = line.getAccountId();
            }
        }

        // Double-Entry Rule Enforcer:
        // The debit and credit accounts must be different.
        if (debitAccountId != null
                && creditAccountId != null
                && debitAccountId.equals(creditAccountId)) {
            throw new AppException(
                    "Debit and Credit accounts must be different."
            );
        }

        long totalDebit = entry.getTotalDebit();
        long totalCredit = entry.getTotalCredit();

        if (totalDebit != totalCredit) {
            throw new AppException(
                    "Debit and credit totals must be equal. Debits are "
                            + Formats.money(totalDebit)
                            + " but credits are "
                            + Formats.money(totalCredit)
                            + "."
            );
        }
    }
}