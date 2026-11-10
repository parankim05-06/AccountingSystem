package ui;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CsvExporter - NEW helper class (does not change any existing code).
 *
 * It does the same job for every Export CSV button:
 *   1. open a Save dialog,
 *   2. write the CSV file,
 *   3. show a success message or an error message.
 *
 * The screens do not call this directly. They call ReportExports, which
 * prepares the headers and rows and then calls exportRows() below.
 */
public class CsvExporter {

    /** Asks where to save, writes the file, and tells the user what happened. */
    public static void exportRows(Component parent, String[] headers,
                                  List<String[]> rows, String defaultFileName) {
        File file = askWhereToSave(parent, defaultFileName);
        if (file == null) {
            return; // the user pressed Cancel - nothing to do
        }
        try {
            writeCsv(file, headers, rows);
            JOptionPane.showMessageDialog(parent,
                    "Export successful!\n\nSaved to:\n" + file.getAbsolutePath(),
                    "Export CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent,
                    "The file could not be saved.\n\nReason: " + ex.getMessage()
                    + "\n\nTip: if the file is open in Excel, close it and try again, "
                    + "or choose a different folder.",
                    "Export CSV failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Shows the Save dialog. Returns null if the user cancels. */
    private static File askWhereToSave(Component parent, String defaultFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), defaultFileName));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        if (file.exists()) {
            int answer = JOptionPane.showConfirmDialog(parent,
                    file.getName() + " already exists.\nDo you want to replace it?",
                    "Export CSV", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        return file;
    }

    /** Writes the file. No windows are opened here, so it is easy to test. */
    public static void writeCsv(File file, String[] headers, List<String[]> rows) throws IOException {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            out.write('\uFEFF'); // lets Excel read the peso sign and other symbols correctly
            writeLine(out, headers);
            for (String[] row : rows) {
                writeLine(out, row);
            }
        }
    }

    private static void writeLine(BufferedWriter out, String[] values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                out.write(',');
            }
            out.write(clean(values[i]));
        }
        out.write("\r\n");
    }

    /**
     * Makes one value safe for a CSV file:
     *  - a value with a comma, quote or new line is wrapped in quotes (quotes are doubled);
     *  - a text starting with = or @ would be run as a formula by Excel, so a ' is put in front.
     */
    public static String clean(String value) {
        if (value == null) {
            return "";
        }
        if (value.startsWith("=") || value.startsWith("@")) {
            value = "'" + value;
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
