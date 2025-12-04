package com.autoshop.app;

import com.toedter.calendar.JCalendar;
import javax.swing.*;
import java.awt.*;
import java.util.Calendar;

public class CalendarCustomizer {

    public static void styleCalendar(JCalendar calendar) {
        JPanel dayPanel = calendar.getDayChooser().getDayPanel();
        calendar.getDayChooser().setDecorationBackgroundVisible(true);

        Font headerFont = new Font("SansSerif", Font.BOLD, 16);
        Dimension comboSize = new Dimension(150, 35);
        Dimension spinnerSize = new Dimension(100, 35);

        // Style Header
        JPanel monthPanel = calendar.getMonthChooser();
        monthPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));

        JComboBox<?> monthCombo = (JComboBox<?>) calendar.getMonthChooser().getComboBox();
        monthCombo.setFont(headerFont);
        monthCombo.setPreferredSize(comboSize);
        ((JLabel)monthCombo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        JPanel yearPanel = calendar.getYearChooser();
        yearPanel.setPreferredSize(spinnerSize);
        JSpinner yearSpinner = (JSpinner) calendar.getYearChooser().getSpinner();
        yearSpinner.setFont(headerFont);
        yearSpinner.setPreferredSize(spinnerSize);
        styleSpinnerTextField(yearSpinner, headerFont);

        // Style Days
        for (Component comp : dayPanel.getComponents()) {
            comp.setFont(new Font("SansSerif", Font.BOLD, 14));
        }
    }

    public static void paintDates(JCalendar calendar) {
        JPanel dayPanel = calendar.getDayChooser().getDayPanel();

        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getDate());
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        // Borders
        javax.swing.border.Border selectedBorder = BorderFactory.createLineBorder(new Color(52, 152, 219), 2);
        javax.swing.border.Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

        for (Component comp : dayPanel.getComponents()) {
            if (comp instanceof JButton btn) {
                String text = btn.getText();

                if (text == null || text.trim().isEmpty() || !text.matches("\\d+")) {
                    btn.setOpaque(false);
                    btn.setContentAreaFilled(false);
                    btn.setBorder(null);
                    continue;
                }

                int day = Integer.parseInt(text);

                btn.setContentAreaFilled(true);
                btn.setOpaque(true);
                btn.setBackground(null);
                btn.setForeground(Color.BLACK);

                // Apply Selection Border
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(calendar.getDate());

                if (selectedCal.get(Calendar.DAY_OF_MONTH) == day &&
                        selectedCal.get(Calendar.MONTH) == currentMonth &&
                        selectedCal.get(Calendar.YEAR) == currentYear) {
                    btn.setBorder(selectedBorder);
                } else {
                    btn.setBorder(emptyBorder);
                }
            }
        }
    }

    private static void styleSpinnerTextField(Container container, Font font) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField tf) {
                tf.setFont(font);
                tf.setHorizontalAlignment(SwingConstants.CENTER);
                return;
            } else if (comp instanceof Container) {
                styleSpinnerTextField((Container) comp, font);
            }
        }
    }

    public static void styleDateChooser(com.toedter.calendar.JDateChooser dateChooser) {
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        dateChooser.setFont(font);

        for (Component comp : dateChooser.getComponents()) {
            if (comp instanceof JButton btn) {
                btn.setPreferredSize(new Dimension(30, 25));
            }
        }
        JCalendar popupCalendar = dateChooser.getJCalendar();
        styleCalendar(popupCalendar);
    }
}