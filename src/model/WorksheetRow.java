package model;

/**
 * One row of the 12-column worksheet. All amounts are in centavos; 0 means
 * "show an empty cell". The last row of the worksheet is named "TOTAL".
 */
public class WorksheetRow {
    public String accountName = "";

    public long trialDebit, trialCredit;
    public long adjustDebit, adjustCredit;
    public long adjustedDebit, adjustedCredit;
    public long incomeDebit, incomeCredit;     // Income Statement columns
    public long equityDebit, equityCredit;     // Owner's Equity columns
    public long balanceDebit, balanceCredit;   // Balance Sheet columns
}
