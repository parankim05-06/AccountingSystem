import database.Database;
import service.AppException;
import service.LegacyImporter;
import ui.WelcomeFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;

/**
 * Where the program starts (Java always begins at main).
 *
 *   1. open/create the SQLite database
 *   2. move any old text-file data into it (only does something once)
 *   3. show the first screen
 */
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

        // Importing old data must never stop the program from starting.
        try {
            String report = new LegacyImporter().importFrom(new File("."));
            if (!report.isEmpty()) {
                System.out.println(report);
            }
        } catch (Exception e) {
            AppException.log("Importing the old text files failed", e);
        }

        SwingUtilities.invokeLater(() -> new WelcomeFrame().setVisible(true));
    }
}
