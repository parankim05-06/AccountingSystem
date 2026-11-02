package model;

/** One row of the accounts table (the chart of accounts). */
public class Account {

    /** The only account types the database accepts. */
    public static final String[] TYPES = {"Asset", "Liability", "Equity", "Revenue", "Expense"};

    private final int id;
    private final String code;
    private final String name;
    private final String type;

    public Account(int id, String code, String name, String type) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
    }

    /**
     * Placeholder shown first in the drop-down lists ("Select account...").
     * Its id is 0 and no real account has id 0, so the service can tell that
     * the user did not choose anything.
     */
    public static Account placeholder() {
        return new Account(0, "", "Select account...", "");
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }

    /** A combo box displays whatever toString() returns. */
    @Override
    public String toString() {
        return code.isEmpty() ? name : code + " - " + name;
    }
}
