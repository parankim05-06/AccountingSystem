import database.Database;
public class make_db_helper {
    public static void main(String[] args) throws Exception {
        Database.initialize();
        System.out.println("Database initialized at: " + Database.getDatabasePath());
    }
}