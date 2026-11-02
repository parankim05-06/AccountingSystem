package service;

import database.Database;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * An error whose message is SAFE and FRIENDLY to show the user, for example
 * "Debit and credit totals must be equal."
 *
 * When there is a technical cause (a SQLException, for example) it is passed in
 * as the second argument. The technical details are then written to the console
 * and to a log file (dapas-error.log, next to the database) so YOU can find the
 * real problem, while the user only sees the friendly message.
 */
public class AppException extends Exception {

    private static final long serialVersionUID = 1L; // Java asks exceptions to have this; ignore it

    public AppException(String userMessage) {
        super(userMessage);
    }

    public AppException(String userMessage, Throwable technicalCause) {
        super(userMessage, technicalCause);
        log(userMessage, technicalCause);
    }

    /** Writes the full technical error to the console and to dapas-error.log. */
    public static void log(String what, Throwable cause) {
        System.err.println("[DAPAS ERROR] " + what);
        cause.printStackTrace();
        try {
            File folder = new File(Database.getDatabasePath()).getParentFile();
            File logFile = new File(folder, "dapas-error.log");
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(LocalDateTime.now() + "  " + what);
                cause.printStackTrace(writer);
                writer.println();
            }
        } catch (Exception ignored) {
            // If even logging fails, the console output above is all we can offer.
        }
    }
}
