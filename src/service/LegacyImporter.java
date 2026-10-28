package service;

import dao.AccountDAO;
import dao.JournalDAO;
import dao.UserDAO;
import model.Account;
import model.JournalEntry;
import model.JournalLine;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * One-time move of the OLD text-file data into SQLite, so nothing you entered
 * with the previous version is lost.
 *
 *   users.txt              ->  users table (passwords are hashed on the way in)
 *   journal_<user>_<name>.txt -> journal_entries + journal_lines (as POSTED entries,
 *                                because the old ledger showed every saved entry)
 *
 * After a file is imported it is RENAMED (users.txt.imported), never deleted, so
 * you still have your original data. Run again later and it finds nothing to do.
 *
 * Old files looked like:   id|dd-MM-yyyy|description|Account,TYPE,debit,credit;Account,TYPE,debit,credit
 */
public class LegacyImporter {

    private static final DateTimeFormatter OLD_DATE = DateTimeFormatter.ofPattern("dd-MM-uuuu");

    private final UserDAO userDAO = new UserDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final JournalDAO journalDAO = new JournalDAO();
    private final Map<String, Integer> accountIdsByName = new HashMap<>(); // small cache

    private int usersImported;
    private int entriesImported;
    private int linesSkipped;

    /** Looks in the folder for old files. Returns a one-line summary, or "" if there was nothing to import. */
    public String importFrom(File folder) {
        File usersFile = new File(folder, "users.txt");
        if (usersFile.isFile()) {
            try {
                importUsers(usersFile);
                markAsImported(usersFile);
            } catch (Exception e) {
                AppException.log("Could not import " + usersFile, e);
            }
        }

        File[] journalFiles = folder.listFiles(
                (dir, name) -> name.startsWith("journal_") && name.endsWith(".txt"));
        if (journalFiles != null) {
            for (File journalFile : journalFiles) {
                try {
                    importJournalFile(journalFile);
                    markAsImported(journalFile);
                } catch (Exception e) {
                    AppException.log("Could not import " + journalFile, e);
                }
            }
        }

        if (usersImported == 0 && entriesImported == 0 && linesSkipped == 0) {
            return "";
        }
        return "Imported " + usersImported + " user(s) and " + entriesImported
             + " journal entr(ies) from the old text files"
             + (linesSkipped > 0 ? " (" + linesSkipped + " damaged line(s) skipped)" : "")
             + ". The old files were renamed to *.imported.";
    }

    private void importUsers(File file) throws Exception {
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String[] parts = line.trim().split(",");
            if (parts.length != 2) continue; // old format: username,password

            String username = parts[0].trim();
            String password = parts[1].trim();
            if (username.isEmpty() || password.isEmpty()) continue;
            if (userDAO.findByUsername(username) != null) continue; // already there

            userDAO.insert(username, AuthService.hashPassword(password), now());
            usersImported++;
        }
    }

    private void importJournalFile(File file) throws Exception {
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            line = line.trim();
            if (line.isEmpty()) continue;

            JournalEntry entry = parseEntry(line);
            if (entry == null) {
                linesSkipped++;
                continue;
            }
            journalDAO.insert(entry);
            entriesImported++;
        }
    }

    /** Turns one old text line into a JournalEntry, or returns null if the line is damaged. */
    private JournalEntry parseEntry(String text) throws Exception {
        String[] parts = text.split("\\|");
        if (parts.length < 4) return null;

        JournalEntry entry = new JournalEntry();
        try {
            entry.setEntryDate(LocalDate.parse(parts[1].trim(), OLD_DATE).toString());
        } catch (Exception e) {
            return null;
        }

        // A description that contained "|" was split into several parts: glue them back.
        StringBuilder description = new StringBuilder(parts[2]);
        for (int i = 3; i < parts.length - 1; i++) {
            description.append("|").append(parts[i]);
        }
        entry.setDescription(description.toString().trim());
        if (entry.getDescription().isEmpty()) return null;

        for (String item : parts[parts.length - 1].split(";")) {
            String[] fields = item.split(",");
            if (fields.length < 4 || fields[0].trim().isEmpty()) return null;

            int accountId = findOrCreateAccount(fields[0].trim(), fields[1].trim());
            long debit = toCentavos(fields[2]);
            long credit = toCentavos(fields[3]);
            entry.addLine(new JournalLine(accountId, debit, credit));
        }

        boolean balanced = entry.getTotalDebit() == entry.getTotalCredit() && entry.getTotalDebit() > 0;
        if (entry.getLines().size() < 2 || !balanced) return null;

        entry.setStatus(JournalEntry.POSTED);
        entry.setCreatedAt(now());
        return entry;
    }

    /** Uses the matching account if we have one, otherwise adds a new account with the next free code. */
    private int findOrCreateAccount(String name, String oldType) throws Exception {
        String key = name.toLowerCase();
        Integer cached = accountIdsByName.get(key);
        if (cached != null) return cached;

        Account existing = accountDAO.findByName(name);
        int id;
        if (existing != null) {
            id = existing.getId();
        } else {
            String type = convertType(oldType);
            id = accountDAO.insert(accountDAO.nextCodeForType(type), name, type);
        }
        accountIdsByName.put(key, id);
        return id;
    }

    /** The old app said INCOME; the new account types say Revenue. */
    private String convertType(String oldType) {
        switch (oldType.toUpperCase()) {
            case "LIABILITY": return "Liability";
            case "EQUITY":    return "Equity";
            case "INCOME":    return "Revenue";
            case "EXPENSE":   return "Expense";
            default:          return "Asset";
        }
    }

    /** Old amounts were doubles like 1500.0. Round to 2 decimals, then to whole centavos. */
    private long toCentavos(String oldAmount) {
        return new BigDecimal(oldAmount.trim()).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }

    private void markAsImported(File file) {
        File target = new File(file.getPath() + ".imported");
        if (target.exists()) {
            target = new File(file.getPath() + "." + System.currentTimeMillis() + ".imported");
        }
        if (!file.renameTo(target)) {
            AppException.log("Imported " + file + " but could not rename it. Please rename it yourself, "
                    + "otherwise it will be imported again.", new Exception("rename failed"));
        }
    }

    private static String now() {
        return LocalDateTime.now().withNano(0).toString();
    }
}
