package ui;

import home.journal;
import home.ledger;
import model.User;

import javax.swing.JFrame;

/**
 * The LEDGER workspace screen.
 * Hosts the layout and functionality of home.ledger.
 */
public class LedgerFrame extends JFrame {

    public LedgerFrame(User user) {
        this(user, JournalFrame.resolveJournalName(user.getUsername()));
    }

    public LedgerFrame(User user, String journalName) {
        setTitle("Accounting System Workspace");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String fileName = "journal_" + user.getUsername() + "_" + journalName + ".txt";
        journal j = new journal(fileName, user.getUsername());
        add(new ledger(j, user.getUsername()));
    }
}
