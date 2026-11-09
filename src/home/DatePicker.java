package home;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A pop-up calendar component. Displays a text field with the selected date
 * and a button to open a month-grid calendar dialog. Future dates are blocked.
 */
public class DatePicker extends JPanel {

    private final JTextField dateField;
    private final JButton calendarButton;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    private Calendar selectedDate;

    public DatePicker() {
        setLayout(new BorderLayout(5, 0));

        selectedDate = Calendar.getInstance();
        dateField = new JTextField(10);
        dateField.setEditable(false);
        dateField.setText(sdf.format(selectedDate.getTime()));

        calendarButton = new JButton("📅");
        calendarButton.setMargin(new Insets(0, 5, 0, 5));
        calendarButton.addActionListener(e -> showCalendar());

        add(dateField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
    }

    private void showCalendar() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Date", true);
        dialog.setLayout(new BorderLayout());

        Calendar cal = (Calendar) selectedDate.clone();

        // Navigation header
        JPanel header = new JPanel(new BorderLayout());
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        JLabel monthYear = new JLabel(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()), SwingConstants.CENTER);

        header.add(prev, BorderLayout.WEST);
        header.add(monthYear, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);

        // Day grid
        JPanel grid = new JPanel(new GridLayout(7, 7));
        String[] days = {"S", "M", "T", "W", "T", "F", "S"};
        for (String day : days) grid.add(new JLabel(day, SwingConstants.CENTER));

        updateGrid(grid, cal, dialog, monthYear);

        prev.addActionListener(e -> {
            cal.add(Calendar.MONTH, -1);
            monthYear.setText(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()));
            updateGrid(grid, cal, dialog, monthYear);
        });

        next.addActionListener(e -> {
            cal.add(Calendar.MONTH, 1);
            monthYear.setText(new SimpleDateFormat("MMMM yyyy").format(cal.getTime()));
            updateGrid(grid, cal, dialog, monthYear);
        });

        // Today link
        JButton todayBtn = new JButton("Today");
        todayBtn.setBorderPainted(false);
        todayBtn.setContentAreaFilled(false);
        todayBtn.setForeground(Color.BLUE);
        todayBtn.addActionListener(e -> {
            selectedDate = Calendar.getInstance();
            dateField.setText(sdf.format(selectedDate.getTime()));
            dialog.dispose();
        });

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(grid, BorderLayout.CENTER);
        dialog.add(todayBtn, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateGrid(JPanel grid, Calendar cal, JDialog dialog, JLabel monthYear) {
        // Clear grid but keep headers (the first 7 components)
        Component[] comps = grid.getComponents();
        for (int i = 7; i < comps.length; i++) grid.remove(comps[i]);

        Calendar temp = (Calendar) cal.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int startDay = temp.get(Calendar.DAY_OF_WEEK) - 1;
        int maxDays = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        Date today = new Date();

        for (int i = 0; i < startDay; i++) grid.add(new JLabel(""));

        for (int d = 1; d <= maxDays; d++) {
            temp.set(Calendar.DAY_OF_MONTH, d);
            JButton dayBtn = new JButton(String.valueOf(d));
            dayBtn.setMargin(new Insets(2, 2, 2, 2));

            if (temp.getTime().after(today)) {
                dayBtn.setEnabled(false);
            } else {
                dayBtn.addActionListener(e -> {
                    selectedDate = (Calendar) temp.clone();
                    dateField.setText(sdf.format(selectedDate.getTime()));
                    dialog.dispose();
                });
            }
            grid.add(dayBtn);
        }
        grid.revalidate();
        grid.repaint();
    }

    public String getDateString() {
        return dateField.getText();
    }

    public void setDate(String dateStr) {
        try {
            selectedDate.setTime(sdf.parse(dateStr));
            dateField.setText(dateStr);
        } catch (Exception e) {
            selectedDate = Calendar.getInstance();
            dateField.setText(sdf.format(selectedDate.getTime()));
        }
    }
}
