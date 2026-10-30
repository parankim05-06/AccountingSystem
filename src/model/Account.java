package model;


public class Account {

    
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

    
    public static Account placeholder() {
        return new Account(0, "", "Select account...", "");
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }

   
    @Override
    public String toString() {
        return code.isEmpty() ? name : code + " - " + name;
    }
}
