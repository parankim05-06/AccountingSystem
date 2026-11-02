package model;

/**
 * One debit or credit line of a journal entry (a row of journal_lines).
 * Amounts are in centavos. A line has EITHER a debit OR a credit, never both.
 *
 * The "display" fields are only filled by queries that JOIN other tables
 * (the ledger and worksheet queries), so screens can show account names
 * without running extra queries.
 */
public class JournalLine {
    private int id;
    private int journalEntryId;
    private final int accountId;
    private final long debit;
    private final long credit;

    // Filled by JOIN queries only
    private String accountCode = "";
    private String accountName = "";
    private String accountType = "";
    private String entryDate = "";
    private String entryDescription = "";

    public JournalLine(int accountId, long debit, long credit) {
        this.accountId = accountId;
        this.debit = debit;
        this.credit = credit;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getJournalEntryId() { return journalEntryId; }
    public void setJournalEntryId(int journalEntryId) { this.journalEntryId = journalEntryId; }
    public int getAccountId() { return accountId; }
    public long getDebit() { return debit; }
    public long getCredit() { return credit; }

    public String getAccountCode() { return accountCode; }
    public String getAccountName() { return accountName; }
    public String getAccountType() { return accountType; }
    public String getEntryDate() { return entryDate; }
    public String getEntryDescription() { return entryDescription; }

    public void setAccountInfo(String code, String name, String type) {
        this.accountCode = code;
        this.accountName = name;
        this.accountType = type;
    }

    public void setEntryInfo(String date, String description) {
        this.entryDate = date;
        this.entryDescription = description;
    }
}
