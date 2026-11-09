import database.Database;
import service.AppException;
import ui.WelcomeFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class Main {

    public static void main(String[] args) {
        try {
            Database.initialize();
        } catch (Exception e) {
            AppException.log("Could not open the database at " + Database.getDatabasePath(), e);
            String message = "The SQLite driver was not found. Put the sqlite-jdbc .jar file in the lib folder.";
            if (e.getMessage() == null || !e.getMessage().contains("No suitable driver")) {
                message = "Unable to open the database:\n" + Database.getDatabasePath()
                        + "\n\nDetails were saved to dapas-error.log in the same folder.";
            }
            JOptionPane.showMessageDialog(null, message, "DAPAS cannot start", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new WelcomeFrame().setVisible(true));
    }
}
