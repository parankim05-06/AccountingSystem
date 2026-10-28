package model;

import java.util.ArrayList;
import java.util.List;

/** One journal transaction (a row of journal_entries) plus its lines. */
public class JournalEntry {
    public static final String DRAFT = "DRAFT";
    public static final String POSTED = "POSTED";

    private int id;
    private String entryDate = "";      // ISO format: 2026-10-27
    private String description = "";
    private String status = DRAFT;
    private String createdAt = "";
    private final List<JournalLine> lines = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEntryDate() { return entryDate; }
    public void setEntryDate(String entryDate) { this.entryDate = entryDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public List<JournalLine> getLines() { return lines; }

    public void addLine(JournalLine line) { lines.add(line); }

    public long getTotalDebit() {
        long total = 0;
        for (JournalLine line : lines) total += line.getDebit();
        return total;
    }

    public long getTotalCredit() {
        long total = 0;
        for (JournalLine line : lines) total += line.getCredit();
        return total;
    }
}
