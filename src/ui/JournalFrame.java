package ui;

import home.journal;
import model.User;

import javax.swing.JFrame;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * The JOURNAL ENTRY workspace screen.
 * Hosts the layout and functionality of home.journal.
 */
public class JournalFrame extends JFrame {

    private final User user;

    public JournalFrame(User user) {
        this(user, resolveJournalName(user.getUsername()));
    }

    public JournalFrame(User user, String journalName) {
        this.user = user;
        setTitle("Accounting System Workspace");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String fileName = "journal_" + user.getUsername() + "_" + journalName + ".txt";
        add(new journal(fileName, user.getUsername()));
    }

    public static String resolveJournalName(String username) {
        File folder = new File(".");
        File[] files = folder.listFiles((dir, name) ->
            name.startsWith("journal_" + username + "_") && name.endsWith(".txt")
        );
        if (files != null && files.length > 0) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            return files[0].getName().replace("journal_" + username + "_", "").replace(".txt", "");
        }
        return "General";
    }
}
