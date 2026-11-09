package ui;

import home.columnar;
import home.journal;
import model.User;

import javax.swing.JFrame;

/**
 * The 12 COLUMNAR SHEET workspace screen.
 * Hosts the layout and functionality of home.columnar.
 */
public class ColumnarFrame extends JFrame {

    public ColumnarFrame(User user) {
        this(user, JournalFrame.resolveJournalName(user.getUsername()));
    }

    public ColumnarFrame(User user, String journalName) {
        setTitle("Accounting System Workspace");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String fileName = "journal_" + user.getUsername() + "_" + journalName + ".txt";
        journal j = new journal(fileName, user.getUsername());
        add(new columnar(j, user.getUsername()));
    }
}
