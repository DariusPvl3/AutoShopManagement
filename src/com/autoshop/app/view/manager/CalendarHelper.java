package com.autoshop.app.view.manager;

import com.toedter.calendar.JCalendar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Function;

public class CalendarHelper {

    /**
     * Attaches a right-click/left-click menu to the days of a JCalendar.
     * @param calendar The calendar to attach to
     * @param countProvider A function that takes a Date and returns the number of appointments (int)
     * @param onViewAction Callback when "View Appointments" is clicked
     * @param onCreateAction Callback when "Create New" is clicked
     */
    public static void attachInteraction(JCalendar calendar,
                                         Function<Date, Integer> countProvider,
                                         Consumer<Date> onViewAction,
                                         Consumer<Date> onCreateAction) {

        JPopupMenu menu = new JPopupMenu();
        JMenuItem viewItem = new JMenuItem("View Appointments");
        JMenuItem createItem = new JMenuItem("Create New Appointment");

        viewItem.setFont(new Font("SansSerif", Font.BOLD, 14));
        createItem.setFont(new Font("SansSerif", Font.BOLD, 14));

        menu.add(viewItem);
        menu.add(new JSeparator());
        menu.add(createItem);

        viewItem.addActionListener(e -> {
            if (onViewAction != null) onViewAction.accept(calendar.getDate());
        });

        createItem.addActionListener(e -> {
            if (onCreateAction != null) onCreateAction.accept(calendar.getDate());
        });

        MouseAdapter clickHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { handle(e); }
            @Override
            public void mouseReleased(MouseEvent e) { handle(e); }

            private void handle(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) || e.isPopupTrigger()) {
                    Component c = e.getComponent();
                    if (c instanceof JButton dayBtn) {
                        String text = dayBtn.getText();
                        // Ignore buttons that aren't days (e.g. navigation)
                        if (text == null || !text.matches("\\d+")) return;

                        // Programmatically select the clicked date
                        java.util.Calendar cal = calendar.getCalendar();
                        cal.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(text));
                        calendar.setDate(cal.getTime());

                        // Update Menu Text based on Data
                        int count = countProvider.apply(calendar.getDate());
                        if (count > 0) {
                            viewItem.setText("View " + count + " Appointment(s)");
                            viewItem.setForeground(new Color(0, 150, 0));
                        } else {
                            viewItem.setText("View Day (Empty)");
                            viewItem.setForeground(Color.GRAY);
                        }
                        menu.show(c, e.getX(), e.getY());
                    }
                }
            }
        };

        // Attach listener to all day buttons
        JPanel dayPanel = calendar.getDayChooser().getDayPanel();
        for (Component comp : dayPanel.getComponents()) {
            if (comp instanceof JButton) {
                comp.addMouseListener(clickHandler);
            }
        }
    }
}